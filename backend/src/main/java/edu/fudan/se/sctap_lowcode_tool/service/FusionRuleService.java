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
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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

    // 事务管理器：用于在后台线程里显式开启事务
    @Autowired
    private PlatformTransactionManager txManager;
    private TransactionTemplate txTemplate;

    // 每次执行过程中的全局节点状态（同一规则执行上下文内使用）
    private final Map<String, Map<String, Object>> globalState = new HashMap<>();

    // 每条主干规则的执行旗标：ruleId -> 是否继续执行
    private final Map<Integer, AtomicBoolean> runningFlags = new ConcurrentHashMap<>();

    // 后台执行线程池
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @PostConstruct
    public void init() {
        this.txTemplate = new TransactionTemplate(txManager);
        this.txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /* =========================
     * 主干规则相关（列表/删除/执行/暂停）
     * ========================= */

    /**
     * 返回主干规则列表（主表仅保留 id/project/name）
     */
    public List<FusionRule> getRuleList() {
        return fusionRuleRepository.findAll();
    }

    /**
     * 删除主干规则（注意：请在 DB 级或应用层确保级联删除其分支或先删分支）
     */
    public boolean deleteRuleById(int ruleId) {
        if (!fusionRuleRepository.existsById(ruleId)) return false;
        fusionRuleRepository.deleteById(ruleId);
        return true;
    }

    /**
     * 执行规则：内部挑一个分支执行（优先 active，其次 branch_index 最小）。
     * 若找不到分支，则返回 false。
     */
    public boolean executeRuleById(int ruleId) {
        FusionRule rule = fusionRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        Optional<FusionRuleBranch> branchOpt = branchRepo.pickOneForExecution(ruleId, null).stream().findFirst();
        if (branchOpt.isEmpty()) {
            System.out.println("ruleId=" + ruleId + " 没有任何分支，无法执行。");
            return false;
        }
        FusionRuleBranch branch = branchOpt.get();

        runningFlags.compute(ruleId, (id, flag) -> {
            if (flag == null || !flag.get()) {
                AtomicBoolean newFlag = new AtomicBoolean(true);
                startRuleLoop(ruleId, branch.getRuleJson(), newFlag, branch.getFusionTarget());
                return newFlag;
            }
            return flag;
        });

        System.out.println("已启动规则持续执行，ruleId=" + ruleId + ", branchId=" + branch.getBranchId());
        return true;
    }

    /**
     * 暂停主干规则（同一主干同时只跑一个分支，因此按 ruleId 停止即可）
     */
    public boolean pauseRuleById(int ruleId) {
        if (!fusionRuleRepository.existsById(ruleId)) return false;
        AtomicBoolean flag = runningFlags.get(ruleId);
        if (flag != null) flag.set(false);
        System.out.println("已暂停规则，ruleId=" + ruleId);
        return true;
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
        // 若 Repository 不含 countBy... 方法，可用 size()
        return branchRepo.findByRule_RuleId(ruleId).size();
    }

    /**
     * 创建分支：branchName 为空则默认“主干名 + index”；index 为当前最大 + 1。
     * spaceId 可为 null（不按空间区分的分支）。
     */
    @Transactional
    public Long createBranch(Integer ruleId,
                             Integer spaceId,
                             String branchName,
                             String fusionTarget,
                             String status,
                             String ruleJson,
                             String flowJson,
                             String remark) {
        FusionRule rule = fusionRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        SpaceInfo space = null;
        if (spaceId != null) {
            space = spaceRepository.findById(spaceId)
                    .orElseThrow(() -> new IllegalArgumentException("Space not found: " + spaceId));
        }

        int nextIdx = branchRepo.findMaxIndex(ruleId, spaceId) + 1;

        FusionRuleBranch b = new FusionRuleBranch();
        b.setRule(rule);
        b.setSpace(space);
        b.setBranchIndex(nextIdx);
        b.setBranchName((branchName == null || branchName.isBlank())
                ? rule.getRuleName() + " " + nextIdx
                : branchName);
        b.setFusionTarget(fusionTarget);
        b.setStatus(status == null ? "inactive" : status);
        b.setRuleJson(ruleJson);
        b.setFlowJson(flowJson);

        return branchRepo.save(b).getBranchId();
    }

    /**
     * 显式执行某个分支（与 /executeBranch/{branchId} 对应）
     */
    public boolean executeBranch(Long branchId) {
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
    public boolean pauseBranch(Long branchId) {
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
    @Transactional
    public boolean deleteBranch(Long branchId) {
        if (!branchRepo.existsById(branchId)) return false;
        branchRepo.deleteById(branchId);
        return true;
    }

    /* =========================
     * 可执行空间计算（基于选中分支的 ruleJson）
     * ========================= */

    /**
     * 计算规则可执行的空间列表：
     * 选一个分支（优先 active，否则最小 index），用其 ruleJson 做能力匹配。
     */
    public List<Integer> getExecutableLocationsForRuleId(int ruleId) {
        FusionRule rule = fusionRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("规则 ID 不存在: " + ruleId));

        Optional<FusionRuleBranch> branchOpt = branchRepo.pickOneForExecution(ruleId, null).stream().findFirst();
        if (branchOpt.isEmpty() || branchOpt.get().getRuleJson() == null) {
            throw new IllegalStateException("该规则没有可用于分析的分支或分支缺少 ruleJson");
        }

        String ruleJsonStr = branchOpt.get().getRuleJson();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode ruleJson;
        try {
            ruleJson = mapper.readTree(ruleJsonStr);
        } catch (Exception e) {
            throw new RuntimeException("解析分支规则 JSON 失败", e);
        }

        List<SpaceInfo> allSpaces = spaceService.findAllSpaces();
        List<Integer> executableLocations = new ArrayList<>();

        for (SpaceInfo space : allSpaces) {
            Integer sid = space.getSpaceId();
            if (canRuleRunInLocation(ruleJson, sid)) {
                executableLocations.add(sid);
            }
        }
        return executableLocations;
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
                    txTemplate.execute(status -> {
                        processNodeRedJson(ruleJson, operatorFlag);
                        if (operatorFlag.getAndSet(false)) {
                            PersonUpdateRequest req = new PersonUpdateRequest();
                            req.setPersonName("mmhu");
                            // 若需要按 space 执行，这里把 spaceId 作为参数往下传并设置
                            nodeRedService.updateFusionTable(effectiveFusionTarget, req);
                        }
                        return null;
                    });
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
        // 如果需要使用设备详情，可在此使用 dr 变量；当前仅示意保留逻辑
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

    @Transactional
    public Map<String, Object> applyRuleToExecutableSpaces(int ruleId, boolean activateNewBranches) {
        // 1) 模板分支（优先 active，否则 index 最小）
        FusionRule rule = fusionRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));
        FusionRuleBranch template = branchRepo.pickOneForExecution(ruleId, null)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("该规则没有可作为模板的分支"));

        // 2) 可达空间
        List<Integer> execSpaces = getExecutableLocationsForRuleId(ruleId);

        List<Integer> createdSpaces = new ArrayList<>();
        List<Integer> skippedSpaces = new ArrayList<>();
        List<Long> createdIds = new ArrayList<>();

        // 3) 逐空间创建（存在则跳过）
        for (Integer spaceId : execSpaces) {
            if (spaceId == null) continue;
            boolean exists = branchRepo.existsByRuleAndSpace(ruleId, spaceId);
            if (exists) {
                skippedSpaces.add(spaceId);
                continue;
            }
            Long bid = createBranch(
                    ruleId,
                    spaceId,
                    null, // 分支名留空 → 内部默认“主干名 + 序号”
                    template.getFusionTarget(),
                    activateNewBranches ? "active" :
                            (template.getStatus() == null ? "inactive" : template.getStatus()),
                    template.getRuleJson(),
                    template.getFlowJson(),
                    "[auto] cloned from ruleId=" + ruleId + " to space=" + spaceId
            );
            createdSpaces.add(spaceId);
            createdIds.add(bid);
        }

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("ruleId", ruleId);
        r.put("activateNewBranches", activateNewBranches);
        r.put("executableSpaces", execSpaces);
        r.put("createdSpaces", createdSpaces);
        r.put("skippedSpaces", skippedSpaces);
        r.put("createdBranchIds", createdIds);
        r.put("createdCount", createdSpaces.size());
        r.put("skippedCount", skippedSpaces.size());
        return r;
    }
}