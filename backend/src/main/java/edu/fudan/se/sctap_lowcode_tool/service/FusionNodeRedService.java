package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.SensorTypeDTO;
import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import edu.fudan.se.sctap_lowcode_tool.model.TslProduct;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvEventRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.TslProductRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FusionNodeRedService {

    private final TslProductRepository productRepository;
    private final EnvEventRepository envEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FusionNodeRedService(
            TslProductRepository productRepository,
            EnvEventRepository envEventRepository
    ) {
        this.productRepository = productRepository;
        this.envEventRepository = envEventRepository;
    }

    /* =====================================================
     * Sensor Event（设备事件）
     * ===================================================== */

    /**
     * Node-RED 事件融合：
     * 查询所有 Sensor Types
     */
    public List<SensorTypeDTO> listSensorTypes() {

        List<TslProduct> products = productRepository.findAll();

        return products.stream()
                .map(this::toSensorTypeDTO)
                .collect(Collectors.toList());
    }

    private SensorTypeDTO toSensorTypeDTO(TslProduct product) {
        List<String> sensingEvents = parseEvents(product.getProductEvent());

        return new SensorTypeDTO(
                product.getProductId(),
                product.getProductName(),
                sensingEvents
        );
    }

    /**
     * 将 product_event JSON 字符串解析成 List<String>
     */
    private List<String> parseEvents(String productEvent) {
        if (productEvent == null || productEvent.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(
                    productEvent,
                    new TypeReference<List<String>>() {}
            );
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /* =====================================================
     * Space Event（环境 / 跨域事件）
     * ===================================================== */

    public List<String> listSpaceEventTypes() {

        List<EnvEvent> events = envEventRepository.findAll();

        return events.stream()
                .map(EnvEvent::getEventType)
                .filter(type -> type != null && !type.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    /* =====================================================
     * Node-RED Rule Upload（DSL 转换）
     * ===================================================== */

    public String handleUploadRule(JsonNode flowJson) {
        System.out.println("FLOW JSON:");
        System.out.println(flowJson.toPrettyString());

        // ---------- 1. 构建 id -> node 映射 ----------
        Map<String, JsonNode> nodeMap = new HashMap<>();
        for (JsonNode node : flowJson) {
            nodeMap.put(node.get("id").asText(), node);
        }

        // ---------- 2. 分类节点 ----------
        List<JsonNode> eventSources = new ArrayList<>();
        List<JsonNode> operators = new ArrayList<>();
        JsonNode publishNode = null;

        for (JsonNode node : flowJson) {
            String type = node.get("type").asText();
            switch (type) {
                case "EventSource" -> eventSources.add(node);
                case "Operator" -> operators.add(node);
                case "Publish" -> publishNode = node;
            }
        }

        // ---------- 3. ruleName ----------
        String spaceEventName = publishNode.get("spaceEventName").asText();
        String ruleName = spaceEventName.replace("事件", "") + "规则";

        Map<String, Object> dsl = new LinkedHashMap<>();
        dsl.put("ruleName", ruleName);

        // ---------- 4. triggers ----------
        List<Map<String, Object>> triggers = new ArrayList<>();

        for (JsonNode es : eventSources) {
            Map<String, Object> trigger = new LinkedHashMap<>();
            String eventSourceType = es.get("eventSourceType").asText();
            trigger.put("eventSource", eventSourceType);

            if ("sensorEvent".equals(eventSourceType)) {
                trigger.put("eventId", es.get("sensingEvent").asText());
            }

            triggers.add(trigger);
        }

        dsl.put("triggers", triggers);

        // ---------- 5. steps（按 wires 顺序） ----------
        List<Map<String, Object>> steps = new ArrayList<>();

        // 从 EventSource 出发
        for (JsonNode es : eventSources) {
            JsonNode wires = es.get("wires");
            if (wires == null || !wires.elements().hasNext()) {
                continue;
            }

            String nextId = wires.get(0).get(0).asText();

            while (nextId != null) {
                JsonNode opNode = nodeMap.get(nextId);
                if (opNode == null || !"Operator".equals(opNode.get("type").asText())) {
                    break;
                }

                Map<String, Object> step = new LinkedHashMap<>();
                step.put("stepId", opNode.get("id").asText());

                String mode = opNode.get("mode").asText();
                step.put("operatorType", mode);

                if ("service".equals(mode)) {
                    step.put("operatorUrl", opNode.get("operatorURL").asText());
                    step.put("operatorHttpMethod", "POST");
                }

                // input
                List<Map<String, Object>> inputList = new ArrayList<>();
                for (JsonNode input : opNode.get("inputsMapping")) {
                    Map<String, Object> in = new LinkedHashMap<>();
                    in.put("key", input.get("key").asText());
                    in.put("type", input.get("type").asText());
                    in.put("desc", input.get("desc").asText());
                    in.put("expr", input.get("source").asText());
                    inputList.add(in);
                }
                step.put("input", inputList);

                // output
                List<Map<String, Object>> outputList = new ArrayList<>();
                JsonNode outputNode = opNode.get("output");
                if (outputNode != null && outputNode.isObject()) {
                    Map<String, Object> out = new LinkedHashMap<>();
                    out.put("key", outputNode.get("key").asText());
                    out.put("type", outputNode.get("type").asText());
                    out.put("desc", outputNode.get("desc").asText());
                    outputList.add(out);
                }
                step.put("output", outputList);

                step.put("next", Collections.emptyList());
                steps.add(step);

                // 找下一个
                JsonNode opWires = opNode.get("wires");
                if (opWires == null || opWires.isEmpty() || opWires.get(0).isEmpty()) {
                    break;
                }
                nextId = opWires.get(0).get(0).asText();
            }
        }

        dsl.put("steps", steps);

        // ---------- 6. publish ----------
        Map<String, Object> publish = new LinkedHashMap<>();
        publish.put("spaceEventId", publishNode.get("id").asText());
        publish.put("spaceEventName", publishNode.get("spaceEventName").asText());
        publish.put("spaceEventDesc", publishNode.get("spaceEventDesc").asText());
        publish.put("condition", publishNode.get("publishCondition").asText());

        List<Map<String, Object>> publishOutputs = new ArrayList<>();
        for (JsonNode out : publishNode.get("outputsMapping")) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("key", out.get("key").asText());
            o.put("type", out.get("type").asText());
            o.put("desc", out.get("description").asText());
            o.put("expr", out.get("source").asText());
            publishOutputs.add(o);
        }
        publish.put("output", publishOutputs);

        dsl.put("publish", publish);

        // ---------- 7. 输出 DSL ----------
        try {
            String dslJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dsl);
            System.out.println("====== DSL CONVERT RESULT ======");
            System.out.println(dslJson);
            return dslJson;
        } catch (Exception e) {
            throw new RuntimeException("DSL 序列化失败", e);
        }
    }
}