package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;
import edu.fudan.se.sctap_lowcode_tool.model.Operator;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.OperatorRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.KafkaConsumerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FusionRuleService {

    @Autowired
    private FusionRuleRepository fusionRuleRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private OperatorService operatorService;

    @Autowired
    private KafkaConsumerUtil kafkaConsumerUtil;

    // 全局状态：Map<String, Map<String,Object>>
    // key: 节点ID
    // value: 一个Map，至少包含 "value" (存数值或布尔结果), "timestamp" (存最近一次更新时间)
    private final Map<String, Map<String, Object>> globalState = new HashMap<>();

    // Executor 服务，用于管理 Kafka 消费者线程
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * 获取所有运算符记录，包括工具类运算符和数据库运算符。
     */
    public List<Operator> getAllOperators() {
        List<Operator> operators = new ArrayList<>();
        operators.addAll(operatorService.getAllUtilOperators()); // 工具类运算符
        operators.addAll(operatorRepository.findAll()); // 数据库中的运算符
        return operators;
    }

    /**
     * 添加或更新规则。
     */
    public void addNewRule(FusionRule fusionRule) {
        FusionRule existRule = fusionRuleRepository.findByRuleName(fusionRule.getRuleName());
        if (existRule != null) {
            fusionRule.setRuleId(existRule.getRuleId());
        }
        fusionRuleRepository.save(fusionRule);
    }

    /**
     * 获取所有规则的列表。
     */
    public List<FusionRule> getRuleList() {
        return fusionRuleRepository.findAll();
    }

    /**
     * 实时处理从 Node-RED 传入的 JSON。
     */
    public void processNodeRedJson(JsonNode ruleJson) {
        if (!ruleJson.has("steps")) {
            System.out.println("规则中未包含有效的步骤信息，跳过处理。");
            return;
        }

        // 获取总步骤数并按顺序处理
        int totalSteps = ruleJson.get("steps").asInt();
        for (int currentStep = 1; currentStep <= totalSteps; currentStep++) {
            List<Map.Entry<String, JsonNode>> currentNodes = findNodesByStep(ruleJson, currentStep);

            for (Map.Entry<String, JsonNode> entry : currentNodes) {
                String nodeId = entry.getKey();
                JsonNode currentNode = entry.getValue();

                String nodeType = currentNode.has("type")
                        ? currentNode.get("type").asText()
                        : "Unknown";

                if ("Sensor".equalsIgnoreCase(nodeType)) {
                    processSensorNode(nodeId, currentNode);
                } else if ("Operator".equalsIgnoreCase(nodeType)) {
                    processOperatorNode(nodeId, currentNode);
                } else {
                    System.out.println("未知的节点类型: " + nodeType + "，跳过该节点。");
                }
            }
        }
    }

    /**
     * 根据 step 查找对应的所有节点。
     */
    private List<Map.Entry<String, JsonNode>> findNodesByStep(JsonNode ruleJson, int step) {
        List<Map.Entry<String, JsonNode>> nodes = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = ruleJson.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode node = entry.getValue();

            // 跳过非节点的字段
            if ("steps".equals(key) || "rulename".equals(key)) {
                continue;
            }

            if (node.has("step") && node.get("step").asInt() == step) {
                nodes.add(entry);
            }
        }
        return nodes;
    }

    /**
     * 处理 Sensor 节点：读取传感器值 + 存入 globalState。
     */
    private void processSensorNode(String nodeId, JsonNode sensorNode) {
        String sensorIdStr = sensorNode.get("sensorId").asText();
        int sensorId = Integer.parseInt(sensorIdStr);

        // 从 Kafka 或其它来源获取传感器值
        double sensorValue = getSensorValue(sensorId);

        // 存入一个 Map
        Map<String, Object> sensorData = new HashMap<>();
        sensorData.put("value", sensorValue);
        // 如果 Node-RED 那边已经带了时间戳，也可以用 sensorNode.get("timestamp"); 这里先用当前时间
        sensorData.put("timestamp", System.currentTimeMillis());

        // 存到全局状态
        globalState.put(nodeId, sensorData);

        System.out.println("从 Sensor 节点获取的值: nodeId="
                + nodeId + "，sensorId=" + sensorId + "，值=" + sensorValue);
    }

    /**
     * 处理 Operator 节点：根据 operatorType + 依赖节点 + (value) 做运算并存结果。
     */
    private void processOperatorNode(String nodeId, JsonNode operatorNode) {
        // 获取 dependencies
        JsonNode dependenciesNode = operatorNode.get("dependencies");
        if (dependenciesNode == null || dependenciesNode.isMissingNode()) {
            System.out.println("Operator 节点没有依赖，跳过处理 nodeId=" + nodeId);
            return;
        }

        List<String> dependencies = new ArrayList<>();
        for (JsonNode dependency : dependenciesNode) {
            dependencies.add(dependency.asText());  // 依赖节点ID
        }

        // 获取 operatorType
        String operatorType = operatorNode.get("operator").asText(); // 假设一定存在
        // 这里的 value 是“最大时间差”或其他辅助数值
        JsonNode valueNode = operatorNode.get("value");
        boolean hasValue = (valueNode != null && !valueNode.isNull());

        // 是否是带时间戳的运算符 (xxx_TIME)
        boolean isTimeOperator = operatorType.endsWith("_TIME");

        // 先准备 input1, input2
        Object input1 = null;
        Object input2 = null;

        // --- 分情况：有 value => 1依赖 + 1个value；没 value => 2 依赖 ---

        if (hasValue) {
            // 需要 1 个依赖 + 1 个 value
            if (dependencies.size() != 1) {
                System.out.println("Operator 节点 " + nodeId
                        + " 需要一个依赖节点和一个 value，但依赖节点数量为 "
                        + dependencies.size());
                return;
            }

            // 拿依赖节点数据
            Map<String, Object> depData = globalState.get(dependencies.get(0));
            if (depData == null) {
                System.out.println("依赖节点 " + dependencies.get(0) + " 无数据，跳过处理。");
                return;
            }

            if (!isTimeOperator) {
                // 不带时间戳 => 普通数值比较
                Double depVal = toDouble(depData.get("value"));
                Double val = valueNode.asDouble(); // Node-RED 里配置的某个阈值
                input1 = depVal;
                input2 = val;
            } else {
                // 带时间戳 => "valueNode" 就是“最大允许时间差”
                Double timeDiff = valueNode.asDouble(); // Node-RED 里写的
                Map<String, Object> depMap = new HashMap<>(depData);
                // depMap.value -> 转为 bool
                Boolean boolVal = (toDouble(depMap.get("value")) != 0.0);
                depMap.put("value", boolVal);

                // depMap 里已经有 "timestamp"
                // 把“最大时间差”存到同一个 Map 里，和之前 maxTimeDiff 类似
                depMap.put("maxTimeDiff", timeDiff);

                // 构造另一个输入 (假设是布尔 true？看具体场景)
                // 如果这个运算是 AND_TIME / OR_TIME，你要比较dep节点 + 另一个节点的同时到达
                // 也许你想在 Node-RED 里注入“第二个节点”的数据?
                // 如果此时只有 “value”=timeDiff，就没有第二个布尔值——那么可以设定：只要 depMap 本身
                // 这样就看你业务:
                //   - 可能不需要第二个节点 => 你也可以把 input2 = null
                //   - 或者你手动创建 valMap, 里面 "value"=true, "timestamp"=now
                // 这里演示：另一个输入是个临时节点
                Map<String, Object> valMap = new HashMap<>();
                valMap.put("value", true);  // 默认 true
                valMap.put("timestamp", System.currentTimeMillis());
                valMap.put("maxTimeDiff", timeDiff);

                input1 = depMap;
                input2 = valMap;
            }

        } else {
            // 没有 value => 需要 2 个依赖
            if (dependencies.size() != 2) {
                System.out.println("Operator 节点 " + nodeId
                        + " 需要两个依赖节点，但依赖节点数量为 " + dependencies.size());
                return;
            }

            Map<String, Object> dep1Data = globalState.get(dependencies.get(0));
            Map<String, Object> dep2Data = globalState.get(dependencies.get(1));

            if (dep1Data == null || dep2Data == null) {
                System.out.println("依赖节点数据缺失，跳过处理 nodeId=" + nodeId);
                return;
            }

            if (!isTimeOperator) {
                // 普通运算
                Double val1 = toDouble(dep1Data.get("value"));
                Double val2 = toDouble(dep2Data.get("value"));
                input1 = val1;
                input2 = val2;
            } else {
                // 带时间戳 => dep1, dep2 都是 Map
                // 但这次 "最大时间差" 没有单独字段 => 说明 Node-RED 只提供了2个依赖, 并没有valueNode
                // 可能 Node-RED 每个依赖自己携带 timeDiff?
                // 或者你的业务就是 AND_TIME / OR_TIME 只比较dep1、dep2的 timestamp 不超出多少?
                // 这里演示一下：如果 Node-RED 不提供，就意味着 timeDiff=0 或一个默认值
                Double defaultTimeDiff = 3000.0; // 你自己决定

                Map<String, Object> dep1Map = new HashMap<>(dep1Data);
                Boolean bool1 = (toDouble(dep1Map.get("value")) != 0.0);
                dep1Map.put("value", bool1);
                dep1Map.put("maxTimeDiff", defaultTimeDiff);

                Map<String, Object> dep2Map = new HashMap<>(dep2Data);
                Boolean bool2 = (toDouble(dep2Map.get("value")) != 0.0);
                dep2Map.put("value", bool2);
                dep2Map.put("maxTimeDiff", defaultTimeDiff);

                input1 = dep1Map;
                input2 = dep2Map;
            }
        }

        // 最终调用 OperatorService
        boolean result = operatorService.applyUtilOperator(operatorType, input1, input2);

        // 将布尔结果转为 double 存储，并更新 "timestamp"
        double operatorDoubleResult = result ? 1.0 : 0.0;
        Map<String, Object> operatorData = new HashMap<>();
        operatorData.put("value", operatorDoubleResult);
        operatorData.put("timestamp", System.currentTimeMillis());

        globalState.put(nodeId, operatorData);

        System.out.println("Operator 节点处理结果: nodeId=" + nodeId
                + "，运算符=" + operatorType
                + "，结果=" + operatorDoubleResult);
    }

    /**
     * 获取传感器值。（伪代码：可从 Kafka 等获取最新的传感器值）
     */
    private double getSensorValue(int sensorId) {
        // 启动 Kafka 消费者线程并获取最新的传感器值
        executorService.execute(kafkaConsumerUtil);

        while (true) {
            String latestMessage = kafkaConsumerUtil.getLatestMessage();
            if (latestMessage != null) {
                try {
                    // 这里 Kafka 返回的是带 "timestamp" 的 JSON
                    JsonNode messageJson = new ObjectMapper().readTree(latestMessage);
                    JsonNode idNode = messageJson.get("id");
                    JsonNode valueNode = messageJson.get("value");

                    int kafkaId = idNode.asInt();
                    double value = valueNode.asDouble();

                    if (kafkaId == sensorId) {
                        System.out.println("从 Kafka 获取到匹配的传感器值: id="
                                + kafkaId + ", value=" + value);
                        return value;
                    }
                } catch (Exception e) {
                    throw new RuntimeException("解析 Kafka 消息失败: " + latestMessage, e);
                }
            }
        }
    }

    /**
     * 工具方法：安全地将 Object 转为 double
     */
    private Double toDouble(Object input) {
        if (input == null) return 0.0;
        if (input instanceof Number) {
            return ((Number) input).doubleValue();
        }
        try {
            return Double.parseDouble(input.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}