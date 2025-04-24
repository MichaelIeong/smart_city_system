package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.PersonUpdateRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceResponse;
import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.KafkaConsumerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.PostConstruct;

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
    private OperatorService operatorService;

    @Autowired
    private KafkaConsumerUtil kafkaConsumerUtil;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private NodeRedService nodeRedService;

    // 事务管理器，用于在后台线程里显式开启事务
    @Autowired
    private PlatformTransactionManager txManager;
    private TransactionTemplate txTemplate;

    // 全局状态 & 空间 ID
    private final Map<String, Map<String, Object>> globalState = new HashMap<>();
    private String spaceId = "";

    // 每条规则的执行旗标：ruleId -> 是否继续执行
    private final Map<Integer, AtomicBoolean> runningFlags = new ConcurrentHashMap<>();

    // 线程池，用于后台不断循环执行
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 构造 TransactionTemplate，在后台线程中手动开启事务
     */
    @PostConstruct
    public void init() {
        this.txTemplate = new TransactionTemplate(txManager);
        // 每次都新开事务，确保读写都在有效 Session 中
        this.txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * 获取所有规则
     */
    public List<FusionRule> getRuleList() {
        return fusionRuleRepository.findAll();
    }

    /**
     * 删除规则
     */
    public boolean deleteRuleById(int ruleId) {
        if (fusionRuleRepository.existsById(ruleId)) {
            fusionRuleRepository.deleteById(ruleId);
            return true;
        }
        return false;
    }

    /**
     * 点“执行”→ 标记 active 并启动后台循环直到暂停
     */
    public boolean executeRuleById(int ruleId) {
        Optional<FusionRule> ruleOpt = fusionRuleRepository.findById(ruleId);
        if (ruleOpt.isEmpty()) return false;

        FusionRule rule = ruleOpt.get();
        rule.setStatus("active");
        fusionRuleRepository.save(rule);

        // 如果还没在跑，就启动
        runningFlags.compute(ruleId, (id, flag) -> {
            if (flag == null || !flag.get()) {
                AtomicBoolean newFlag = new AtomicBoolean(true);
                startRuleLoop(ruleId, rule.getRuleJson(), newFlag);
                return newFlag;
            }
            return flag;
        });

        System.out.println("已启动规则持续执行，ruleId=" + ruleId);
        return true;
    }

    /**
     * 真正的后台循环：每次都通过 txTemplate 开新事务
     */
    private void startRuleLoop(int ruleId, String ruleJsonStr, AtomicBoolean runningFlag) {
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
                    // 在新事务里执行一次完整流程
                    txTemplate.execute(status -> {
                        processNodeRedJson(ruleJson, operatorFlag);

                        // 若 operator 触发，则更新 fusion table
                        if (operatorFlag.getAndSet(false)) {
                            fusionRuleRepository.findById(ruleId).ifPresent(freshRule -> {
                                PersonUpdateRequest req = new PersonUpdateRequest();
                                req.setPersonName("mmhu");
                                req.setSpaceId(spaceId);
                                nodeRedService.updateFusionTable(freshRule.getFusionTarget(), req);
                            });
                        }
                        return null;  // 必须 return
                    });

                    // 轮询间隔（可按需调整）
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
     * 点“暂停”→ 标记 inactive 并让后台循环自然退出
     */
    public boolean pauseRuleById(int ruleId) {
        Optional<FusionRule> ruleOpt = fusionRuleRepository.findById(ruleId);
        if (ruleOpt.isEmpty()) return false;

        FusionRule rule = ruleOpt.get();
        rule.setStatus("inactive");
        fusionRuleRepository.save(rule);

        // 置 false 让循环结束
        AtomicBoolean flag = runningFlags.get(ruleId);
        if (flag != null) {
            flag.set(false);
        }

        System.out.println("已暂停规则，ruleId=" + ruleId);
        return true;
    }

    /**
     * 解析并执行 Node-RED 规则
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
        // 在事务上下文内安全地取到 DTO，且不再泄露任何懒加载代理
        DeviceResponse dr = deviceService.findByDeviceId(String.valueOf(sensorId))
                .orElseThrow(() -> new RuntimeException("Device not found"));
        spaceId = dr.getSpaceId();

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
        if (input instanceof Number) {
            return ((Number) input).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(input));
        } catch (Exception e) {
            return 0.0;
        }
    }
}