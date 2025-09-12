package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.PersonUpdateRequest;
import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;
import edu.fudan.se.sctap_lowcode_tool.model.FusionRuleBranch;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleBranchRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.KafkaConsumerUtil;
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

    // 每次执行过程中的全局节点状态（同一规则执行上下文内使用）
    private final Map<String, Map<String, Object>> globalState = new HashMap<>();

    // 每条主干规则的执行旗标：ruleId -> 是否继续执行
    private final Map<Integer, AtomicBoolean> runningFlags = new ConcurrentHashMap<>();

    // 后台执行线程池
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /* =========================
     * 主干规则相关
     * ========================= */

    /**
     * 返回主干规则列表（主表仅保留 id/project/name）
     */
    public List<FusionRule> getRuleList() {
        return fusionRuleRepository.findAll();
    }

    /**
     * 删除主干规则：先查出并逐条删除其分支，再删除主干本身。
     * 全程不走批量 JPQL，避免 “Executing an update/delete query” 异常。
     */
    public boolean deleteRuleById(int ruleId) {
        if (!fusionRuleRepository.existsById(ruleId)) {
            return false;
        }
        // 1) 先删子：逐条删分支（避免 @Modifying 批量更新需要事务）
        List<FusionRuleBranch> branches = branchRepo.findByRule_RuleId(ruleId);
        if (branches != null && !branches.isEmpty()) {
            for (FusionRuleBranch b : branches) {
                // 用 deleteById 最稳妥（走实体删除）
                branchRepo.deleteById(b.getBranchId());
            }
        }
        // 2) 再删父：删除主干
        fusionRuleRepository.deleteById(ruleId);
        return true;
    }

    /**
     * 主干改名
     */
    public boolean updateRuleName(int ruleId, String newName) {
        return fusionRuleRepository.findById(ruleId).map(r -> {
            r.setRuleName(newName);
            fusionRuleRepository.save(r);
            return true;
        }).orElse(false);
    }

    /* =========================
     * 分支 CRUD / 执行
     * ========================= */

    /**
     * 根据主干列出分支
     */
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
     * 按可执行空间创建分支；spaceIds == null 表示对全部可执行空间处理；
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

        for (Integer sid : spaceIds) {
            try {
                SpaceInfo space = spaceRepository.findById(sid)
                        .orElseThrow(() -> new IllegalArgumentException("空间不存在: " + sid));

                FusionRuleBranch branch = new FusionRuleBranch();
                branch.setRule(rule);
                branch.setSpace(space);

                // 不使用 index（确保表结构允许 NULL 或有默认值）
                // branch.setBranchIndex(null);

                // 分支名 = 可达空间返回的 name（此处即 SpaceInfo.spaceName）
                String sname = Optional.ofNullable(space.getSpaceName()).orElse("").trim();
                branch.setBranchName(sname.isEmpty() ? ("空间#" + sid) : sname);

                branch.setStatus(activateNewBranches ? "active" : "inactive");
                // 如需：branch.setFusionTarget(...) / setRuleJson(...) / setFlowJson(...)

                branchRepo.saveAndFlush(branch);

                created.add(Map.of(
                        "branchId", branch.getBranchId(),
                        "spaceId", sid,
                        "spaceName", branch.getBranchName()
                ));
            } catch (Exception ex) {
                errors.add(Map.of(
                        "spaceId", sid,
                        "error", ex.getClass().getSimpleName() + ": " + ex.getMessage()
                ));
            }
        }

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
        System.out.println("已暂停分支，branchId=" + branchId + ", ruleId=" + ruleId);
        return true;
    }

    /**
     * 删除分支
     */
    public boolean deleteBranch(int branchId) {
        if (!branchRepo.existsById(branchId)) return false;
        branchRepo.deleteById(branchId);
        return true;
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
        boolean isTimeOp = opType.endsWith("_TIME");

        Object in1, in2;
        if (hasVal) {
            if (deps.size() != 1) {
                System.out.println("Operator " + nodeId + " 依赖数不符");
                return;
            }
            Map<String, Object> depData = globalState.get(deps.get(0));
            if (depData == null) return;

            if (!isTimeOp) {
                in1 = toDouble(depData.get("value"));
                in2 = valNode.asDouble();
            } else {
                double diff = valNode.asDouble();
                Map<String, Object> a = new HashMap<>(depData);
                a.put("value", toDouble(a.get("value")) != 0.0);
                a.put("maxTimeDiff", diff);

                Map<String, Object> b = new HashMap<>();
                b.put("value", true);
                b.put("timestamp", System.currentTimeMillis());
                b.put("maxTimeDiff", diff);

                in1 = a;
                in2 = b;
            }
        } else {
            if (deps.size() != 2) {
                System.out.println("Operator " + nodeId + " 依赖数不符");
                return;
            }
            Map<String, Object> d1 = globalState.get(deps.get(0));
            Map<String, Object> d2 = globalState.get(deps.get(1));
            if (d1 == null || d2 == null) return;

            if (!isTimeOp) {
                in1 = toDouble(d1.get("value"));
                in2 = toDouble(d2.get("value"));
            } else {
                double defDiff = 3000.0;
                Map<String, Object> a = new HashMap<>(d1);
                a.put("value", toDouble(a.get("value")) != 0.0);
                a.put("maxTimeDiff", defDiff);

                Map<String, Object> b = new HashMap<>(d2);
                b.put("value", toDouble(b.get("value")) != 0.0);
                b.put("maxTimeDiff", defDiff);

                in1 = a;
                in2 = b;
            }
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
}