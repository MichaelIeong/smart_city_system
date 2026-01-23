package edu.fudan.se.sctap_lowcode_tool.execution;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

public class ServiceGraph {
    // 所有的节点（Key是step的ID）
    private final Map<String, ServiceTaskNode> nodes;
    // JSON里的 action_params 部分
    private final JsonNode inputsDefinition;

    public ServiceGraph(Map<String, ServiceTaskNode> nodes, JsonNode inputsDefinition) {
        this.nodes = nodes;
        this.inputsDefinition = inputsDefinition;
    }

    public Map<String, ServiceTaskNode> getNodes() { return nodes; }
    public JsonNode getInputsDefinition() { return inputsDefinition; }
}