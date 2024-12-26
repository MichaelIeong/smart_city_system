package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.model.*;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.OperatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FusionRuleService {

    @Autowired
    private FusionRuleRepository fusionRuleRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private OperatorService operatorService;

    // 全局状态存储，用于保存每一步的结果 (step -> value)
    private Map<String, Double> globalState = new HashMap<>();

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
     * 调用工具类运算符的逻辑。
     *
     * @param operatorName 运算符名称
     * @param input1       第一个输入值
     * @param input2       第二个输入值
     * @return 运算结果
     */
    public boolean applyUtilOperator(String operatorName, Object input1, Object input2) {
        return operatorService.applyUtilOperator(operatorName, input1, input2);
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
            JsonNode currentNode = findNodeByStep(ruleJson, currentStep);

            if (currentNode == null) {
                System.out.println("未找到 step=" + currentStep + " 的节点，跳过该步骤。");
                continue;
            }

            String nodeType = currentNode.get("type").asText();
            switch (nodeType) {
                case "Sensor":
                    processSensorNode(currentNode);
                    break;
                case "Operator":
                    processOperatorNode(currentNode);
                    break;
                default:
                    System.out.println("未知的节点类型: " + nodeType + "，跳过该步骤。");
                    break;
            }
        }
    }

    /**
     * 根据 step 查找对应的节点。
     *
     * @param ruleJson JSON 对象
     * @param step     当前步骤
     * @return 对应的节点，如果未找到返回 null
     */
    private JsonNode findNodeByStep(JsonNode ruleJson, int step) {
        for (Iterator<Map.Entry<String, JsonNode>> it = ruleJson.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            JsonNode node = entry.getValue();
            if (node.has("step") && node.get("step").asInt() == step) {
                return node;
            }
        }
        return null;
    }

    /**
     * 处理 Sensor 节点。
     *
     * @param sensorNode Sensor 节点
     */
    private void processSensorNode(JsonNode sensorNode) {
        String step = sensorNode.get("step").asText();
        String location = sensorNode.get("location").asText();
        String sensingFunction = sensorNode.get("sensingFunction").asText();

        // 获取传感器值
        double sensorValue = getSensorValue(sensorNode);
        globalState.put(step, sensorValue);

        System.out.println("从 Sensor 节点获取的值: step=" + step + "，位置=" + location + "，功能=" + sensingFunction + "，值=" + sensorValue);
    }

    /**
     * 处理 Operator 节点。
     *
     * @param operatorNode Operator 节点
     */
    private void processOperatorNode(JsonNode operatorNode) {
        String step = operatorNode.get("step").asText();
        String previousStep = String.valueOf(Integer.parseInt(step) - 1); // Operator 依赖上一节点的值

        Double inputValue = globalState.get(previousStep);

        if (inputValue == null) {
            System.out.println("未找到对应的输入值，无法处理 Operator 节点: step=" + step);
            return;
        }

        String operatorName = operatorNode.get("operator").asText();
        double operatorValue = operatorNode.get("value").asDouble();

        // 调用 OperatorService 进行计算
        boolean result;
        try {
            result = operatorService.applyUtilOperator(operatorName, inputValue, operatorValue);
        } catch (UnsupportedOperationException e) {
            System.out.println("不支持的运算符: " + operatorName + "，跳过处理。");
            return;
        }

        System.out.println("规则计算结果: step=" + step + ", Sensor Value (" + inputValue + ") " +
                operatorName + " Operator Value (" + operatorValue + ") = " + result);

        // 将结果存入全局状态 (布尔值可以存为 1.0 或 0.0)
        globalState.put(step, result ? 1.0 : 0.0);
    }

    /**
     * 获取传感器值
     *
     * @param sensorNode 包含传感器信息的 JSON 节点
     * @return 模拟的传感器值
     */
    private double getSensorValue(JsonNode sensorNode) {

        return 26; // mock 值
    }
}