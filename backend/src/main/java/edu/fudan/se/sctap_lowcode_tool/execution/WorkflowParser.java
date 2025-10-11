package edu.fudan.se.sctap_lowcode_tool.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * WorkflowParser
 * 负责解析 Node-RED 导出的 JSON，生成 DAG 的数据结构
 */
@Component
public class WorkflowParser {

    // 节点 id -> 节点 JSON
    private final Map<String, JsonNode> nodeMap = new HashMap<>();

    // 节点 id -> 后继节点 id 列表
    private final Map<String, List<String>> dependencies = new HashMap<>();

    // 起始节点 id
    private String startNodeId;

    public void initParser(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        if (!root.isArray()) {
            throw new IllegalArgumentException("传入的工作流 JSON 必须是数组");
        }

        nodeMap.clear();
        dependencies.clear();
        startNodeId = null;

        // 第一步：收集所有节点
        for (JsonNode node : root) {
            if (!node.has("id") || !node.has("type")) continue;

            String nodeId = node.get("id").asText();
            String type = node.get("type").asText();

            nodeMap.put(nodeId, node);

            // 找起始节点（Start 或 Composition）
            if (("start".equalsIgnoreCase(type) || "Composition".equals(type)) && startNodeId == null) {
                startNodeId = nodeId;
            }
        }

        // 第二步：建立依赖关系
        for (JsonNode node : root) {
            if (!node.has("id") || !node.has("wires")) continue;

            String nodeId = node.get("id").asText();
            JsonNode wires = node.get("wires");

            if (wires.isArray()) {
                for (JsonNode wireGroup : wires) {
                    if (wireGroup.isArray()) {
                        for (JsonNode targetNode : wireGroup) {
                            String targetId = targetNode.asText();
                            dependencies
                                    .computeIfAbsent(nodeId, k -> new ArrayList<>())
                                    .add(targetId);
                        }
                    }
                }
            }
        }

        if (startNodeId == null) {
            throw new RuntimeException("未找到起始节点（需要 type = start 或 Composition）");
        }

        System.out.println("WorkflowParser 初始化完成");
        System.out.println("起始节点: " + startNodeId);
        System.out.println("节点数: " + nodeMap.size());
    }

    public Map<String, JsonNode> getNodeMap() {
        return nodeMap;
    }

    public Map<String, List<String>> getDependencies() {
        return dependencies;
    }

    public String getStartNodeId() {
        return startNodeId;
    }
}