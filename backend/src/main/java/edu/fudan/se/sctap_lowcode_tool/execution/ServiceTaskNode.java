package edu.fudan.se.sctap_lowcode_tool.execution;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;

public class ServiceTaskNode {
    private String id;
    private JsonNode rawNode; 
    private List<String> nextIds = new ArrayList<>();
    private Set<String> dependencies = new HashSet<>();

    // 基础的 Getter/Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public JsonNode getRawNode() { return rawNode; }
    public void setRawNode(JsonNode rawNode) { this.rawNode = rawNode; }
    public List<String> getNextIds() { return nextIds; }
    public Set<String> getDependencies() { return dependencies; }
}