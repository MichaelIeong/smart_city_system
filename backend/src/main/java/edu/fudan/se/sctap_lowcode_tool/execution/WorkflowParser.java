package edu.fudan.se.sctap_lowcode_tool.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.util.*;

@Component  // 让 Spring 负责管理 WorkflowParser
public class WorkflowParser {

    private final Map<String, JsonNode> nodeMap = new HashMap<>();
    private final Map<String, List<String>> dependencies = new HashMap<>();
    private final Map<String, Integer> pendingDependencies = new HashMap<>();
    private String startNodeId;

    // 无参构造方法（Spring 需要）
    public WorkflowParser() {
    }

    // 解析 JSON 并初始化数据
    public void initParser(String json) throws Exception {
        nodeMap.clear();
        dependencies.clear();
        pendingDependencies.clear();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);

        for (JsonNode node : rootNode) {
            String nodeId = node.get("id").asText();
            nodeMap.put(nodeId, node);

            if ("start".equals(node.get("type").asText())) {
                startNodeId = nodeId;
            }

            JsonNode wiresNode = node.get("wires");
            if (wiresNode != null && wiresNode.isArray()) {
                for (JsonNode wireArray : wiresNode) {
                    for (JsonNode targetNode : wireArray) {
                        String targetId = targetNode.asText();
                        dependencies.computeIfAbsent(nodeId, k -> new ArrayList<>()).add(targetId);
                        pendingDependencies.put(targetId, pendingDependencies.getOrDefault(targetId, 0) + 1);
                    }
                }
            }
        }
    }

    public String getStartNodeId() {
        return startNodeId;
    }

    public Map<String, JsonNode> getNodeMap() {
        return nodeMap;
    }

    public Map<String, List<String>> getDependencies() {
        return dependencies;
    }

    public Map<String, Integer> getPendingDependencies() {
        return pendingDependencies;
    }
}