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

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    // 全局状态存储结构
    private final Map<String, Map<String, Object>> globalState = new HashMap<>();

    // 用于 Kafka 消费的线程池
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Autowired
    private PersonService personService;
    @Autowired
    private NodeRedService nodeRedService;

    private String spaceId = "";

    private boolean operatorflag = false;
    /**
     * 获取规则列表
     */
    public List<FusionRule> getRuleList() {
        return fusionRuleRepository.findAll();
    }

    /**
     * 根据 ID 删除规则
     */
    public boolean deleteRuleById(int ruleId) {
        Optional<FusionRule> ruleOpt = fusionRuleRepository.findById(ruleId);
        if (ruleOpt.isPresent()) {
            fusionRuleRepository.deleteById(ruleId);
            return true;
        }
        return false;
    }

    /**
     * 点击“执行”→ 激活并执行规则
     */
    public boolean executeRuleById(int ruleId) {
        Optional<FusionRule> ruleOpt = fusionRuleRepository.findById(ruleId);
        if (ruleOpt.isPresent()) {
            FusionRule rule = ruleOpt.get();
            rule.setStatus("active");
            fusionRuleRepository.save(rule);

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode ruleJson = mapper.readTree(rule.getRuleJson());
                processNodeRedJson(ruleJson);
                System.out.println("已执行并激活规则，ruleId=" + ruleId);
                //构造更新表
                if(operatorflag){
                    PersonUpdateRequest personUpdateRequest = new PersonUpdateRequest();
                    personUpdateRequest.setPersonName("mmhu");
                    personUpdateRequest.setSpaceId(spaceId);
                    nodeRedService.updateFusionTable(rule.getFusionTarget(), personUpdateRequest);
                }
                return true;
            } catch (Exception e) {
                throw new RuntimeException("执行失败：" + e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 点击“暂停”→ 将规则设为 inactive
     */
    public boolean pauseRuleById(int ruleId) {
        Optional<FusionRule> ruleOpt = fusionRuleRepository.findById(ruleId);
        if (ruleOpt.isPresent()) {
            FusionRule rule = ruleOpt.get();
            rule.setStatus("inactive");
            fusionRuleRepository.save(rule);
            System.out.println("已将规则设为 inactive，ruleId=" + ruleId);
            return true;
        }
        return false;
    }

    /**
     * 实时处理 Node-RED 的规则 JSON
     */
    public void processNodeRedJson(JsonNode ruleJson) {
        if (!ruleJson.has("steps")) {
            System.out.println("规则中未包含有效的步骤信息，跳过处理。");
            return;
        }

        int totalSteps = ruleJson.get("steps").asInt();
        for (int currentStep = 1; currentStep <= totalSteps; currentStep++) {
            List<Map.Entry<String, JsonNode>> currentNodes = findNodesByStep(ruleJson, currentStep);
            for (Map.Entry<String, JsonNode> entry : currentNodes) {
                String nodeId = entry.getKey();
                JsonNode currentNode = entry.getValue();
                String nodeType = currentNode.has("type") ? currentNode.get("type").asText() : "Unknown";

                switch (nodeType) {
                    case "Sensor"   -> processSensorNode(nodeId, currentNode);
                    case "Operator" -> processOperatorNode(nodeId, currentNode);
                    default         -> System.out.println("未知的节点类型: " + nodeType + "，跳过该节点。");
                }
            }
        }
    }

    private List<Map.Entry<String, JsonNode>> findNodesByStep(JsonNode ruleJson, int step) {
        List<Map.Entry<String, JsonNode>> nodes = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = ruleJson.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode node = entry.getValue();

            if ("steps".equals(key) || "rulename".equals(key)) {
                continue;
            }

            if (node.has("step") && node.get("step").asInt() == step) {
                nodes.add(entry);
            }
        }
        return nodes;
    }

    private void processSensorNode(String nodeId, JsonNode sensorNode) {
        int sensorId = Integer.parseInt(sensorNode.get("sensorId").asText());
        spaceId = deviceService.findByDeviceId(String.valueOf(sensorId))
                .map(DeviceResponse::getSpaceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        double sensorValue = getSensorValue(sensorId);

        Map<String, Object> sensorData = new HashMap<>();
        sensorData.put("value", sensorValue);
        sensorData.put("timestamp", System.currentTimeMillis());
        globalState.put(nodeId, sensorData);

        System.out.println("从 Sensor 节点获取的值: nodeId=" + nodeId +
                           "，sensorId=" + sensorId + "，值=" + sensorValue);
    }

    private void processOperatorNode(String nodeId, JsonNode operatorNode) {
        JsonNode dependenciesNode = operatorNode.get("dependencies");
        if (dependenciesNode == null || dependenciesNode.isMissingNode()) {
            System.out.println("Operator 节点没有依赖，跳过处理 nodeId=" + nodeId);
            return;
        }

        List<String> dependencies = new ArrayList<>();
        for (JsonNode dep : dependenciesNode) {
            dependencies.add(dep.asText());
        }

        String operatorType = operatorNode.get("operator").asText();
        JsonNode valueNode = operatorNode.get("value");
        boolean hasValue = (valueNode != null && !valueNode.isNull());
        boolean isTimeOperator = operatorType.endsWith("_TIME");

        Object input1;
        Object input2;

        if (hasValue) {
            if (dependencies.size() != 1) {
                System.out.println("Operator 节点 " + nodeId +
                                   " 需要一个依赖节点和一个 value，但依赖节点数量为 " +
                                   dependencies.size());
                return;
            }

            Map<String, Object> depData = globalState.get(dependencies.get(0));
            if (depData == null) {
                System.out.println("依赖节点 " + dependencies.get(0) + " 无数据，跳过处理。");
                return;
            }

            if (!isTimeOperator) {
                input1 = toDouble(depData.get("value"));
                input2 = valueNode.asDouble();
            } else {
                double timeDiff = valueNode.asDouble();
                Map<String, Object> depMap = new HashMap<>(depData);
                depMap.put("value", toDouble(depMap.get("value")) != 0.0);
                depMap.put("maxTimeDiff", timeDiff);

                Map<String, Object> valMap = new HashMap<>();
                valMap.put("value", true);
                valMap.put("timestamp", System.currentTimeMillis());
                valMap.put("maxTimeDiff", timeDiff);

                input1 = depMap;
                input2 = valMap;
            }

        } else {
            if (dependencies.size() != 2) {
                System.out.println("Operator 节点 " + nodeId +
                                   " 需要两个依赖节点，但数量为 " +
                                   dependencies.size());
                return;
            }

            Map<String, Object> dep1Data = globalState.get(dependencies.get(0));
            Map<String, Object> dep2Data = globalState.get(dependencies.get(1));
            if (dep1Data == null || dep2Data == null) {
                System.out.println("依赖节点数据缺失，跳过处理 nodeId=" + nodeId);
                return;
            }

            if (!isTimeOperator) {
                input1 = toDouble(dep1Data.get("value"));
                input2 = toDouble(dep2Data.get("value"));
            } else {
                double defaultTimeDiff = 3000.0;

                Map<String, Object> dep1Map = new HashMap<>(dep1Data);
                dep1Map.put("value", toDouble(dep1Map.get("value")) != 0.0);
                dep1Map.put("maxTimeDiff", defaultTimeDiff);

                Map<String, Object> dep2Map = new HashMap<>(dep2Data);
                dep2Map.put("value", toDouble(dep2Map.get("value")) != 0.0);
                dep2Map.put("maxTimeDiff", defaultTimeDiff);

                input1 = dep1Map;
                input2 = dep2Map;
            }
        }

        boolean result = operatorService.applyUtilOperator(operatorType, input1, input2);
        if(result){
            operatorflag = true;
        }
        double operatorDoubleResult = result ? 1.0 : 0.0;

        Map<String, Object> operatorData = new HashMap<>();
        operatorData.put("value", operatorDoubleResult);
        operatorData.put("timestamp", System.currentTimeMillis());
        globalState.put(nodeId, operatorData);

        System.out.println("Operator 节点处理结果: nodeId=" + nodeId +
                           "，运算符=" + operatorType + "，结果=" + operatorDoubleResult);
    }

    private double getSensorValue(int sensorId) {
        String latestMessage = kafkaConsumerUtil.getLatestMessageBySensorId(sensorId);
        if (latestMessage != null) {
            try {
                JsonNode messageJson = new ObjectMapper().readTree(latestMessage);
                return messageJson.get("value").asDouble();
            } catch (Exception e) {
                throw new RuntimeException("解析 Kafka 消息失败: " + latestMessage, e);
            }
        }
        throw new RuntimeException("未能在 Kafka 中找到 sensorId=" + sensorId + " 的最新值");
    }

    private Double toDouble(Object input) {
        if (input == null) {
            return 0.0;
        }
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