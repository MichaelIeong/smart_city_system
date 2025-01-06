package edu.fudan.se.sctap_lowcode_tool.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class JsonUtil {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 将 JSON 字符串解析为对象列表
     *
     * @param json  JSON 字符串
     * @param clazz 目标类类型
     * @param <T>   泛型类型
     * @return 解析后的对象列表
     */
    public <T> List<T> parseJsonToList(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 将对象列表转换为 JSON 字符串
     *
     * @param list 对象列表
     * @param <T>  泛型类型
     * @return 转换后的 JSON 字符串
     */
    public <T> String convertListToJson(List<T> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 根据 step 顺序解析 JSON，返回排序后的节点列表。
     *
     * @param ruleJson 包含规则的 JSON 对象
     * @return 按 step 顺序排序的节点列表
     */
    public List<JsonNode> getNodesByStep(JsonNode ruleJson) {
        List<JsonNode> sortedNodes = new ArrayList<>();

        if (!ruleJson.has("steps")) {
            return sortedNodes;
        }

        int totalSteps = ruleJson.get("steps").asInt();

        // 按 step 顺序依次查找节点
        for (int currentStep = 1; currentStep <= totalSteps; currentStep++) {
            JsonNode currentNode = findNodeByStep(ruleJson, currentStep);
            if (currentNode != null) {
                sortedNodes.add(currentNode);
            }
        }

        return sortedNodes;
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
     * 将 JSON 字符串解析为 JsonNode
     *
     * @param json JSON 字符串
     * @return JsonNode 对象
     */
    public JsonNode parseJsonToNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将 JsonNode 转换为 JSON 字符串
     *
     * @param node JsonNode 对象
     * @return JSON 字符串
     */
    public String convertNodeToJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}