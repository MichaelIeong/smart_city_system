package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.PersonUpdateRequest;
import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;
import edu.fudan.se.sctap_lowcode_tool.model.FusionRuleBranch;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleBranchRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.KafkaConsumerUtil;
import edu.fudan.se.sctap_lowcode_tool.utils.OperatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class FusionRuleService {

    @Autowired
    private FusionRuleRepository fusionRuleRepository;
    @Autowired
    private FusionRuleBranchRepository branchRepo;

    @Autowired
    private OperatorService operatorService;
    @Autowired
    private KafkaConsumerUtil kafkaConsumerUtil;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private NodeRedService nodeRedService;
    @Autowired
    private SpaceService spaceService;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private FusionRuleRecommendService fusionRuleRecommendService;

    private final Map<String, Map<String, Object>> globalState = new HashMap<>();

    // 每条主干规则的执行旗标：ruleId -> 是否继续执行
    private final Map<Integer, AtomicBoolean> runningFlags = new ConcurrentHashMap<>();

    // 后台执行线程池
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /* =========================
     * 主干规则相关
     * ========================= */

    public List<FusionRule> getRuleList() {
        return fusionRuleRepository.findAll();
    }

    /**
     * 删除主干规则，同时删除向量库中的对应规则条目。
     */
    public boolean deleteRuleById(int ruleId) {
        if (!fusionRuleRepository.existsById(ruleId)) {
            return false;
        }

        // 先删 Milvus 中的规则向量
        fusionRuleRecommendService.deleteRuleVector(ruleId);

        // 再逐条删除分支
        List<FusionRuleBranch> branches = branchRepo.findByRule_RuleId(ruleId);
        if (branches != null && !branches.isEmpty()) {
            for (FusionRuleBranch b : branches) {
                branchRepo.deleteById(b.getBranchId());
            }
        }
        // 最后删除主干
        fusionRuleRepository.deleteById(ruleId);
        return true;
    }

    /**
     * 主干改名后，同步更新 Milvus 中规则语义。
     */
    public boolean updateRuleName(int ruleId, String newName) {
        return fusionRuleRepository.findById(ruleId).map(r -> {
            r.setRuleName(newName);
            FusionRule saved = fusionRuleRepository.save(r);
            fusionRuleRecommendService.upsertRule(saved);
            return true;
        }).orElse(false);
    }

    public boolean updateBranchName(int branchId, String newName) {
        return branchRepo.findById(branchId).map(b -> {
            b.setBranchName(newName);
            branchRepo.save(b);
            // 分支名称也体现到规则语义里，这里顺带刷新一下规则向量
            fusionRuleRecommendService.upsertRuleByBranch(b);
            return true;
        }).orElse(false);
    }

    /* =========================
     * 分支 CRUD / 执行
     * ========================= */

    public List<FusionRuleBranch> listBranchesByRule(Integer ruleId) {
        return branchRepo.findByRule_RuleId(ruleId);
    }

    /**
     * 统计某主干下分支数量（供控制器展示用）
     */
    public long countBranchesOfRule(Integer ruleId) {
        return branchRepo.findByRule_RuleId(ruleId).size();
    }

    /**
     * 把主干规则应用到指定空间列表：为每个空间创建分支。
     * 创建完成后，用新的分支集更新一次 Milvus 中的规则向量。
     */
    public Map<String, Object> applyRuleToExecutableSpaces(int ruleId,
                                                           boolean activateNewBranches,
                                                           List<Integer> spaceIds) {
        if (spaceIds == null || spaceIds.isEmpty()) {
            throw new IllegalArgumentException("spaceIds 不能为空");
        }

        Map<String, Object> out = new HashMap<>();
        List<Map<String, Object>> created = new ArrayList<>();
        List<Map<String, Object>> errors = new ArrayList<>();

        FusionRule rule = fusionRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("规则未找到: " + ruleId));

        FusionRuleBranch template = branchRepo.pickOneForExecution(ruleId, null)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("该规则没有可用于拷贝的分支（缺少 ruleJson/flowJson）"));

        final String baseFusionTarget = template.getFusionTarget();
        final String baseRuleJson = template.getRuleJson();
        final String baseFlowJson = template.getFlowJson();
        final ObjectMapper mapper = new ObjectMapper();

        for (Integer sid : spaceIds) {
            try {
                SpaceInfo space = spaceRepository.findById(sid)
                        .orElseThrow(() -> new IllegalArgumentException("空间不存在: " + sid));

                String spaceName = Optional.ofNullable(space.getSpaceName()).orElse("").trim();
                if (spaceName.isEmpty()) spaceName = "空间#" + sid;

                String ruleJsonForSpace = baseRuleJson;
                String flowJsonForSpace = baseFlowJson;

                // 1) 替换 ruleJson 里的 location
                if (ruleJsonForSpace != null && !ruleJsonForSpace.isBlank()) {
                    JsonNode root;
                    try {
                        root = mapper.readTree(ruleJsonForSpace);
                    } catch (Exception e) {
                        throw new IllegalStateException("ruleJson 解析失败，无法套用到空间: " + sid, e);
                    }

                    // 1) 深度遍历：替换所有 key=location 的文本值为当前 spaceName（你原逻辑保留）
                    Deque<JsonNode> stack = new ArrayDeque<>();
                    stack.push(root);
                    while (!stack.isEmpty()) {
                        JsonNode cur = stack.pop();
                        if (cur.isObject()) {
                            ObjectNode obj = (ObjectNode) cur;
                            Iterator<Map.Entry<String, JsonNode>> it = obj.fields();
                            List<Map.Entry<String, JsonNode>> snapshot = new ArrayList<>();
                            it.forEachRemaining(snapshot::add);
                            for (Map.Entry<String, JsonNode> e : snapshot) {
                                String key = e.getKey();
                                JsonNode val = e.getValue();
                                if ("location".equals(key) && val != null && val.isTextual()) {
                                    obj.put(key, spaceName);
                                } else {
                                    stack.push(val);
                                }
                            }
                        } else if (cur.isArray()) {
                            ArrayNode arr = (ArrayNode) cur;
                            for (int i = 0; i < arr.size(); i++) stack.push(arr.get(i));
                        }
                    }

                    // 2) 关键：按 space 重映射所有 Sensor.sensorId
                    //    A) 最简单：该 space 取一个 sensorId，所有 Sensor 节点都用它
                    Integer mappedSensorId = deviceService.pickSensorIdBySpace(sid)
                            .orElseThrow(() -> new IllegalStateException("空间 " + sid + " 下找不到任何传感器设备，无法重映射 sensorId"));

                    root.fields().forEachRemaining(entry -> {
                        String k = entry.getKey();
                        if ("steps".equals(k) || "rulename".equals(k)) return;

                        JsonNode node = entry.getValue();
                        if (!(node instanceof ObjectNode obj)) return;

                        String type = obj.path("type").asText("");
                        if ("Sensor".equalsIgnoreCase(type)) {
                            obj.put("sensorId", mappedSensorId);
                        }
                    });

                    ruleJsonForSpace = mapper.writeValueAsString(root);
                }
                // 3) 替换 flowJson 里的 location（如你之前逻辑），并尝试重映射（可选但建议）
                if (flowJsonForSpace != null && !flowJsonForSpace.isBlank()) {
                    JsonNode root;
                    try {
                        root = mapper.readTree(flowJsonForSpace);
                    } catch (Exception e) {
                        throw new IllegalStateException("flowJson 解析失败，无法套用到空间: " + sid, e);
                    }

                    Deque<JsonNode> stack = new ArrayDeque<>();
                    stack.push(root);
                    while (!stack.isEmpty()) {
                        JsonNode cur = stack.pop();
                        if (cur.isObject()) {
                            ObjectNode obj = (ObjectNode) cur;
                            Iterator<Map.Entry<String, JsonNode>> it = obj.fields();
                            List<Map.Entry<String, JsonNode>> snapshot = new ArrayList<>();
                            it.forEachRemaining(snapshot::add);

                            // 替换 location
                            for (Map.Entry<String, JsonNode> e : snapshot) {
                                String key = e.getKey();
                                JsonNode val = e.getValue();
                                if ("location".equals(key) && val != null && val.isTextual()) {
                                    obj.put(key, spaceName);
                                } else {
                                    stack.push(val);
                                }
                            }

                            // 尝试重映射：如果 flow 里也带有 type/sensingFunction/function/id
                            String type = obj.path("type").asText("");
                            if ("Sensor".equalsIgnoreCase(type)) {
                                String sensingFunc = obj.path("sensingFunction").asText(null);
                                if (sensingFunc != null && !sensingFunc.isBlank()) {
                                    deviceService.pickSensorIdBySpace(sid)
                                            .ifPresent(mid -> obj.put("sensorId", mid));
                                }
                            }
                            if ("Actuator".equalsIgnoreCase(type)) {
                                String func = obj.path("function").asText(null);
                                if (func != null && !func.isBlank()) {
                                    deviceService.pickSensorIdBySpace(sid)
                                            .ifPresent(mid -> {
                                                if (obj.has("deviceId")) obj.put("deviceId", mid);
                                                else if (obj.has("actuatorId")) obj.put("actuatorId", mid);
                                                else obj.put("deviceId", mid);
                                            });
                                }
                            }
                        } else if (cur.isArray()) {
                            ArrayNode arr = (ArrayNode) cur;
                            for (int i = 0; i < arr.size(); i++) stack.push(arr.get(i));
                        }
                    }

                    flowJsonForSpace = mapper.writeValueAsString(root);
                }

                // 4) 保存新分支
                FusionRuleBranch branch = new FusionRuleBranch();
                branch.setRule(rule);
                branch.setSpace(space);
                String autoName = extractLocationName(ruleJsonForSpace);
                branch.setBranchName(autoName);
                branch.setFusionTarget(baseFusionTarget);
                branch.setStatus(activateNewBranches ? "active" : "inactive");
                branch.setRuleJson(ruleJsonForSpace);
                branch.setFlowJson(flowJsonForSpace);

                branchRepo.saveAndFlush(branch);

                created.add(Map.of(
                        "branchId", branch.getBranchId(),
                        "spaceId", sid,
                        "spaceName", spaceName
                ));
            } catch (Exception ex) {
                errors.add(Map.of(
                        "spaceId", sid,
                        "error", ex.getClass().getSimpleName() + ": " + ex.getMessage()
                ));
            }
        }

        fusionRuleRecommendService.upsertRule(rule);

        out.put("createdBranches", created.size());
        out.put("created", created);
        out.put("errors", errors);
        return out;
    }
    /* =========================
     * 分支规则相关
     * ========================= */

    /**
     * 显式执行某个分支（与 /executeBranch/{branchId} 对应）
     */
    public boolean executeBranch(int branchId) {
        FusionRuleBranch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + branchId));
        int ruleId = branch.getRule().getRuleId();

        branch.setStatus("active");
        branchRepo.save(branch);

        runningFlags.compute(ruleId, (id, flag) -> {
            if (flag == null || !flag.get()) {
                AtomicBoolean newFlag = new AtomicBoolean(true);
                startRuleLoop(ruleId, branch.getRuleJson(), newFlag, branch.getFusionTarget());
                return newFlag;
            }
            return flag;
        });

        System.out.println("已启动分支执行，ruleId=" + ruleId + ", branchId=" + branchId);
        return true;
    }

    /**
     * 暂停某个分支（由于按 ruleId 控制执行，暂停等价于暂停该主干）
     */
    public boolean pauseBranch(int branchId) {
        FusionRuleBranch branch = branchRepo.findById(branchId).orElse(null);
        if (branch == null) return false;
        int ruleId = branch.getRule().getRuleId();
        AtomicBoolean flag = runningFlags.get(ruleId);
        if (flag != null) flag.set(false);
        branch.setStatus("inactive");
        branchRepo.save(branch);
        System.out.println("已暂停分支，branchId=" + branchId + ", ruleId=" + ruleId);
        return true;
    }

    /**
     * 删除分支后，规则语义可能变化，触发一次规则向量更新。
     */
    public boolean deleteBranch(int branchId) {
        return branchRepo.findById(branchId).map(branch -> {
            FusionRule rule = branch.getRule();
            int ruleId = rule != null ? rule.getRuleId() : -1;
            branchRepo.delete(branch);
            if (rule != null) {
                fusionRuleRecommendService.upsertRule(rule);
            }
            System.out.println("已删除分支，branchId=" + branchId + ", ruleId=" + ruleId);
            return true;
        }).orElse(false);
    }

    /**
     * 计算规则可执行的空间列表：
     * 选一个分支（优先 active，否则最小 index），用其 ruleJson 做能力匹配。
     */
    public List<Map<String, Object>> getExecutableSpaces(int ruleId) {
        FusionRuleBranch probe = branchRepo.pickOneForExecution(ruleId, null)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("该规则没有可用于分析的分支或分支缺少 ruleJson"));

        JsonNode ruleJson;
        try {
            ruleJson = new ObjectMapper().readTree(probe.getRuleJson());
        } catch (Exception e) {
            throw new RuntimeException("解析分支规则 JSON 失败", e);
        }

        List<SpaceInfo> allSpaces = spaceService.findAllSpaces();
        return allSpaces.stream()
                .filter(sp -> sp.getSpaceId() != null)
                .filter(sp -> canRuleRunInLocation(ruleJson, sp.getSpaceId()))                  // 能力匹配
                .filter(sp -> !branchRepo.existsByRuleAndSpace(ruleId, sp.getSpaceId()))       // 仅此处过滤“已存在”
                .map(sp -> Map.<String, Object>of("id", sp.getSpaceId(), "name", sp.getSpaceName()))
                .collect(Collectors.toList());
    }

    /* =========================
     * 后台执行主循环 & 规则节点处理
     * ========================= */

    /**
     * 后台循环：每次在新事务里跑一遍流程；命中 operatorFlag 则更新 fusion table
     */
    private void startRuleLoop(int ruleId, String ruleJsonStr, AtomicBoolean runningFlag, String effectiveFusionTarget) {
        executorService.submit(() -> {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode ruleJson;
            try {
                ruleJson = mapper.readTree(ruleJsonStr);
            } catch (Exception e) {
                System.err.println("解析规则 JSON 失败：" + e.getMessage());
                return;
            }
            AtomicBoolean operatorFlag = new AtomicBoolean(false);

            while (runningFlag.get()) {
                try {
                    processNodeRedJson(ruleJson, operatorFlag);
                    if (operatorFlag.getAndSet(false)) {
                        PersonUpdateRequest req = new PersonUpdateRequest();
                        req.setPersonName("mmhu");
                        nodeRedService.updateFusionTable(effectiveFusionTarget, req);
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ex) {
                    System.err.println("规则执行出错：" + ex.getMessage());
                }
            }
            System.out.println("规则执行循环结束，ruleId=" + ruleId);
        });
    }

    /**
     * 解析并执行 Node-RED 风格规则
     */
    public void processNodeRedJson(JsonNode ruleJson, AtomicBoolean operatorFlag) {
        if (!ruleJson.has("steps")) {
            System.out.println("规则中未包含 steps，跳过。");
            return;
        }
        int total = ruleJson.get("steps").asInt();
        for (int step = 1; step <= total; step++) {
            findNodesByStep(ruleJson, step).forEach(entry -> {
                String nodeId = entry.getKey();
                JsonNode node = entry.getValue();
                String type = node.path("type").asText("Unknown");

                switch (type) {
                    case "Sensor" -> processSensorNode(nodeId, node);
                    case "Operator" -> processOperatorNode(nodeId, node, operatorFlag);
                    default -> System.out.println("未知节点类型: " + type);
                }
            });
        }
    }

    private boolean canRuleRunInLocation(JsonNode ruleJson, Integer spaceId) {
        Set<String> requiredActuatingFunctions = new HashSet<>();
        Set<String> requiredSensingFunctions = new HashSet<>();

        ruleJson.fields().forEachRemaining(entry -> {
            JsonNode node = entry.getValue();
            if (!node.has("type")) return;
            String type = node.get("type").asText();

            if ("Actuator".equalsIgnoreCase(type)) {
                String func = node.path("function").asText();
                if (func != null && !func.isBlank()) {
                    requiredActuatingFunctions.add(func);
                }
            } else if ("Sensor".equalsIgnoreCase(type)) {
                String func = node.path("sensingFunction").asText();
                if (func != null && !func.isBlank()) {
                    requiredSensingFunctions.add(func);
                }
            }
        });

        Set<String> availableFunctions = deviceService.getActuatingFunctionNamesBySpace(spaceId);
        boolean hasAllActuating = availableFunctions.containsAll(requiredActuatingFunctions);
        boolean hasAllSensing = availableFunctions.containsAll(requiredSensingFunctions);
        return hasAllActuating && hasAllSensing;
    }

    private List<Map.Entry<String, JsonNode>> findNodesByStep(JsonNode ruleJson, int step) {
        List<Map.Entry<String, JsonNode>> list = new ArrayList<>();
        ruleJson.fields().forEachRemaining(e -> {
            if (!"steps".equals(e.getKey()) && !"rulename".equals(e.getKey())) {
                if (e.getValue().path("step").asInt(-1) == step) {
                    list.add(e);
                }
            }
        });
        return list;
    }

    private void processSensorNode(String nodeId, JsonNode sensorNode) {
        int sensorId = sensorNode.path("sensorId").asInt();
        DeviceResponse dr = deviceService.findByDeviceIdFromMySQL(String.valueOf(sensorId))
                .orElseThrow(() -> new RuntimeException("Device not found"));

        double value = getSensorValue(sensorId);
        Map<String, Object> data = new HashMap<>();
        data.put("value", value);
        data.put("timestamp", System.currentTimeMillis());
        globalState.put(nodeId, data);

        System.out.println("Sensor 节点 " + nodeId + " 值=" + value);
    }

    private void processOperatorNode(String nodeId, JsonNode opNode, AtomicBoolean operatorFlag) {
        JsonNode depsNode = opNode.path("dependencies");
        if (!depsNode.isArray()) {
            System.out.println("Operator " + nodeId + " 缺少 dependencies");
            return;
        }

        List<String> deps = new ArrayList<>();
        depsNode.forEach(n -> deps.add(n.asText()));
        String opType = opNode.path("operator").asText();
        JsonNode valNode = opNode.get("value");
        boolean hasVal = valNode != null && !valNode.isNull();

        Object in1, in2;
        if (OperatorUtil.TIME_FILTER.equals(opType)) {
            // 统一时间过滤器：把 value 原样交给 OperatorService；第二个入参传 nodeId
            in1 = valNode;     // 可能是对象/JSON字符串，OperatorService 会自行解析
            in2 = nodeId;      // 用作 COUNTDOWN 的 key
        } else if (hasVal) {
            if (deps.size() != 1) {
                System.out.println("Operator " + nodeId + " 依赖数不符");
                return;
            }
            Map<String, Object> depData = globalState.get(deps.get(0));
            if (depData == null) return;

            in1 = toDouble(depData.get("value"));
            in2 = (valNode.isNumber() ? valNode.asDouble() : toDouble(valNode.asText()));
        } else {
            if (deps.size() != 2) {
                System.out.println("Operator " + nodeId + " 依赖数不符");
                return;
            }
            Map<String, Object> d1 = globalState.get(deps.get(0));
            Map<String, Object> d2 = globalState.get(deps.get(1));
            if (d1 == null || d2 == null) return;

            in1 = toDouble(d1.get("value"));
            in2 = toDouble(d2.get("value"));
        }

        boolean res = operatorService.applyUtilOperator(opType, in1, in2);
        if (res) operatorFlag.set(true);
        double out = res ? 1.0 : 0.0;

        Map<String, Object> outData = Map.of(
                "value", out,
                "timestamp", System.currentTimeMillis()
        );
        globalState.put(nodeId, outData);

        System.out.println("Operator " + nodeId + " 结果=" + out);
    }

    private double getSensorValue(int sensorId) {
        String msg = kafkaConsumerUtil.getLatestMessageBySensorId(sensorId);
        if (msg != null) {
            try {
                JsonNode j = new ObjectMapper().readTree(msg);
                return j.path("value").asDouble();
            } catch (Exception e) {
                throw new RuntimeException("解析 Kafka 消息失败: " + msg, e);
            }
        }
        throw new RuntimeException("找不到 sensorId=" + sensorId + " 的最新消息");
    }

    private Double toDouble(Object input) {
        if (input instanceof Number) return ((Number) input).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(input));
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 根据分支ID获取该分支的 ruleJson / flowJson
     */
    public Map<String, Object> getBranchJson(int branchId) {
        FusionRuleBranch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("分支未找到: " + branchId));

        Map<String, Object> res = new HashMap<>();
        res.put("branchId", branch.getBranchId());
        res.put("ruleId", branch.getRule() != null ? branch.getRule().getRuleId() : null);
        res.put("branchName", branch.getBranchName());
        res.put("spaceId", branch.getSpace() != null ? branch.getSpace().getSpaceId() : null);
        res.put("status", branch.getStatus());
        res.put("fusionTarget", branch.getFusionTarget());
        res.put("ruleJson", branch.getRuleJson());
        res.put("flowJson", branch.getFlowJson());
        return res;
    }

    /**
     * 上传 / 修改某个分支的 ruleJson / flowJson 后，同步刷新对应规则的向量。
     * 这就是你说的“upload 一条 rule 时触发更新 Milvus”的关键入口。
     */
    public boolean updateBranchJson(int branchId, String ruleJson, String flowJson) {
        return branchRepo.findById(branchId).map(b -> {
            if (ruleJson != null) {
                b.setRuleJson(ruleJson);
            }
            if (flowJson != null) {
                b.setFlowJson(flowJson);
            }
            FusionRuleBranch saved = branchRepo.save(b);
            fusionRuleRecommendService.onBranchRuleUploaded(saved);
            return true;
        }).orElse(false);
    }

    private String extractLocationName(String ruleJsonStr) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(ruleJsonStr);

            Set<String> locations = new LinkedHashSet<>();

            root.fields().forEachRemaining(entry -> {
                JsonNode node = entry.getValue();
                if (node.has("type") && "Sensor".equalsIgnoreCase(node.get("type").asText())) {
                    JsonNode loc = node.get("location");
                    if (loc != null && loc.isTextual() && !loc.asText().isBlank()) {
                        locations.add(loc.asText());
                    }
                }
            });

            if (locations.isEmpty()) return "未命名实例";
            return String.join(" + ", locations);
        } catch (Exception e) {
            return "未命名实例";
        }
    }
}