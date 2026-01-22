package edu.fudan.se.sctap_lowcode_tool.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class CompositeServiceParser {
    private final ObjectMapper mapper = new ObjectMapper();

    public ServiceGraph parse(String json) throws Exception {
        JsonNode root = mapper.readTree(json);
        Map<String, ServiceTaskNode> nodes = new HashMap<>();

        // 1. 遍历 steps 数组
        JsonNode steps = root.get("steps");
        for (JsonNode step : steps) {
            ServiceTaskNode node = new ServiceTaskNode();
            node.setId(step.get("id").asText());
            node.setRawNode(step); 
            nodes.put(node.getId(), node);
        }

        // 2. 建立依赖关系
        for (JsonNode step : steps) {
            String currentId = step.get("id").asText();
            JsonNode nexts = step.get("next");
            for (JsonNode next : nexts) {
                String nextId = next.asText();
                if (nodes.containsKey(nextId)) {
                    nodes.get(nextId).getDependencies().add(currentId);
                    nodes.get(currentId).getNextIds().add(nextId);
                }
            }
        }

        return new ServiceGraph(nodes, root.get("inputs"));
    }
}