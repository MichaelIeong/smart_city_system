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

    // 全局状态存储，用于保存每个节点的 ID 和对应的值
    private final Map<String, Double> globalState = new HashMap<>();

    // Executor 服务，用于管理 Kafka 消费者线程
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * 获取所有运算符记录，包括工具类运算符和数据库运算符。
     *
     * @return 所有运算符的列表
     */
    public List<Operator> getAllOperators() {
        List<Operator> operators = new ArrayList<>();
        operators.addAll(operatorService.getAllUtilOperators()); // 工具类运算符
        operators.addAll(operatorRepository.findAll()); // 数据库中的运算符
        return operators;
    }

    /**
     * 添加或更新规则。
     *
     * @param fusionRule 要添加或更新的规则对象
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
     *
     * @return 数据库中所有规则的列表
     */
    public List<FusionRule> getRuleList() {
        return fusionRuleRepository.findAll();
    }

    /**
     * 实时处理从 Node-RED 传入的 JSON。
     *
     * @param ruleJson 包含规则信息的 JSON 对象
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

                String nodeType = currentNode.has("type") ? currentNode.get("type").asText() : "Unknown";
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
     *
     * @param ruleJson JSON 对象
     * @param step     当前步骤
     * @return 对应的节点列表，如果未找到返回空列表
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
     * 处理 Sensor 节点。
     *
     * @param nodeId     Sensor 节点的 ID
     * @param sensorNode Sensor 节点的 JSON 数据
     */
    private void processSensorNode(String nodeId, JsonNode sensorNode) {
        String sensorIdStr = sensorNode.get("sensorId").asText(); // 假设 sensorId 一定存在且正确解析
        int sensorId = Integer.parseInt(sensorIdStr);

        double sensorValue = getSensorValue(sensorId);  // 获取传感器值
        globalState.put(nodeId, sensorValue);  // 使用节点的 ID 作为键存储值

        System.out.println("从 Sensor 节点获取的值: nodeId=" + nodeId + "，sensorId=" + sensorId + "，值=" + sensorValue);
    }

    /**
     * 处理 Operator 节点。
     *
     * @param nodeId      Operator 节点的 ID
     * @param operatorNode Operator 节点的 JSON 数据
     */
    private void processOperatorNode(String nodeId, JsonNode operatorNode) {
        JsonNode dependenciesNode = operatorNode.get("dependencies");
        if (dependenciesNode == null || dependenciesNode.isMissingNode()) {
            System.out.println("Operator 节点没有依赖，跳过处理 nodeId=" + nodeId);
            return;
        }

        List<String> dependencies = new ArrayList<>();
        for (JsonNode dependency : dependenciesNode) {
            dependencies.add(dependency.asText());  // 获取前置节点的 ID
        }

        String operatorType = operatorNode.get("operator").asText(); // 假设 operator 字段存在
        JsonNode valueNode = operatorNode.get("value");
        boolean hasValue = valueNode != null && !valueNode.isNull();

        List<Object> inputs = new ArrayList<>();

        if (hasValue) {
            // 运算符需要一个依赖和一个 value
            if (dependencies.size() != 1) {
                System.out.println("Operator 节点 " + nodeId + " 需要一个依赖节点和一个 value，但依赖节点数量为 " + dependencies.size());
                return;
            }

            Double dependencyResult = globalState.get(dependencies.get(0));
            inputs.add(dependencyResult);
            inputs.add(valueNode.asDouble());
        } else {
            // 运算符需要两个依赖
            if (dependencies.size() != 2) {
                System.out.println("Operator 节点 " + nodeId + " 需要两个依赖节点，但依赖节点数量为 " + dependencies.size());
                return;
            }

            Double dep1 = globalState.get(dependencies.get(0));
            Double dep2 = globalState.get(dependencies.get(1));

            inputs.add(dep1);
            inputs.add(dep2);
        }

        // 调用 OperatorService 进行运算
        boolean result = operatorService.applyUtilOperator(operatorType, inputs.get(0), inputs.get(1));
        double operatorResult = result ? 1.0 : 0.0;
        globalState.put(nodeId, operatorResult);  // 存储结果

        System.out.println("Operator 节点处理结果: nodeId=" + nodeId + "，结果=" + operatorResult);
    }

    /**
     * 获取传感器值。
     *
     * @param sensorId 传感器设备 ID
     * @return 从 Kafka 获取到的传感器值
     */
    private double getSensorValue(int sensorId) {
        // 启动 Kafka 消费者线程并获取最新的传感器值
        executorService.execute(kafkaConsumerUtil);

        while (true) {
            String latestMessage = kafkaConsumerUtil.getLatestMessage();
            if (latestMessage != null) {
                try {
                    JsonNode messageJson = new ObjectMapper().readTree(latestMessage);  // 将 JSON 字符串解析为 JsonNode

                    JsonNode idNode = messageJson.get("id");
                    JsonNode valueNode = messageJson.get("value");

                    // 假设 Kafka 消息中的 id 和 value 总是存在且正确
                    int kafkaId = idNode.asInt();
                    double value = valueNode.asDouble();

                    if (kafkaId == sensorId) {
                        System.out.println("从 Kafka 获取到匹配的传感器值: id=" + kafkaId + ", value=" + value);
                        return value; // 成功匹配到传感器 ID，返回传感器值
                    }
                } catch (Exception e) {
                    // 将受检异常转换为运行时异常，简化代码
                    throw new RuntimeException("解析 Kafka 消息失败: " + latestMessage, e);
                }
            }
        }
    }
}