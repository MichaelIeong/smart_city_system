package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.SensorTypeDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule;
import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import edu.fudan.se.sctap_lowcode_tool.model.EnvEventGrid;
import edu.fudan.se.sctap_lowcode_tool.model.TslProduct;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvEventGridRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvEventRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.TslProductRepository;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.EventFusionRuleService;
import edu.fudan.se.sctap_lowcode_tool.DTO.ProductEventDTO;
import edu.fudan.se.sctap_lowcode_tool.model.ProductEvent;
import edu.fudan.se.sctap_lowcode_tool.repository.ProductEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FusionNodeRedService {

    private final TslProductRepository productRepository;
    private final EnvEventRepository envEventRepository;
    private final EnvEventGridRepository envEventGridRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventFusionRuleService eventFusionRuleService;
    private final ProductEventRepository productEventRepository;

    public FusionNodeRedService(
        TslProductRepository productRepository,
        EnvEventRepository envEventRepository,
        EnvEventGridRepository envEventGridRepository,
        EventFusionRuleService eventFusionRuleService,
        ProductEventRepository productEventRepository
    ) {
        this.productRepository = productRepository;
        this.envEventRepository = envEventRepository;
        this.envEventGridRepository = envEventGridRepository;
        this.eventFusionRuleService = eventFusionRuleService;
        this.productEventRepository = productEventRepository;
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
    * Product Event（产品级事件）
    * ===================================================== */

    public List<ProductEventDTO> listProductEvents() {

        List<ProductEvent> productEvents = productEventRepository.findAll();

        return productEvents.stream()
                .map(pe -> new ProductEventDTO(
                        pe.getProductEvent(),  // product_event
                        pe.getEventName()      // event_name
                ))
                .collect(Collectors.toList());
    }

    /* =====================================================
     * Node-RED Rule Upload
     * ===================================================== */

    public void handleUploadRule(JsonNode flowJson) {
        JsonNode publishNode = null;
        JsonNode eventSourceNode = null;

        for (JsonNode node : flowJson) {
            String type = node.get("type").asText();
            if ("Publish".equals(type)) {
                publishNode = node;
            }
            if ("EventSource".equals(type)) {
                eventSourceNode = node;
            }
        }

        if (publishNode == null) {
            throw new IllegalArgumentException("Publish node not found in flowJson");
        }
        if (eventSourceNode == null) {
            throw new IllegalArgumentException("EventSource node not found in flowJson");
        }

        // ---------- 提取基础字段 ----------
        String spaceEventName = publishNode.get("spaceEventName").asText();
        String spaceEventType = publishNode.get("spaceEventType").asText();
        String spaceEventDesc = publishNode.get("spaceEventDesc").asText();

        // ---------- 构建 eventJson ----------
        String eventJson = buildEventJson(publishNode);

        // ---------- 构建 DSL ----------
        EventFusionRule ruleDsl = buildDSL(flowJson);

        // ---------- 判断 crossRegion ----------
        boolean crossRegion = true;
        if (eventSourceNode.has("gridId")) {
            String gridId = eventSourceNode.get("gridId").asText();
            crossRegion = "crossRegion".equals(gridId);
        }

        // ---------- 提取 projectId ----------
        Integer projectId = null;
        if (eventSourceNode.has("projectId") && !eventSourceNode.get("projectId").isNull()) {
            String pid = eventSourceNode.get("projectId").asText();
            if (!pid.isBlank()) {
                projectId = Integer.valueOf(pid);
            }
        }

        // ---------- 解析关联设备ID ----------
        List<String> deviceIds = new ArrayList<>();

        String eventSourceType = eventSourceNode.get("eventSourceType").asText();
        if ("sensorEvent".equals(eventSourceType)) {
            if (eventSourceNode.has("sensorType")) {
                JsonNode sensorTypeNode = eventSourceNode.get("sensorType");
                if (sensorTypeNode.isArray()) {
                    for (JsonNode s : sensorTypeNode) {
                        deviceIds.add(s.asText());
                    }
                } else {
                    deviceIds.add(sensorTypeNode.asText());
                }
            }
        }

        // ---------- 组装并入库 EnvEvent ----------
        EnvEvent envEvent = new EnvEvent();
        envEvent.setEventType(spaceEventType);
        envEvent.setEventName(spaceEventName);
        envEvent.setDescription(spaceEventDesc);
        envEvent.setEventJson(eventJson);
        envEvent.setCrossRegion(crossRegion);
        envEvent.setCreateTime(LocalDateTime.now());
        envEvent.setRuleDsl(ruleDsl);
        envEvent.setDependDtypes(deviceIds);
        envEvent.setProjectId(projectId);

        EnvEvent savedEvent = envEventRepository.save(envEvent);
        Long envEventId = savedEvent.getId();

        // ---------- 若非跨网格，组装并入库 EnvEventGrid ----------
        if (!crossRegion) {
            String gridId = eventSourceNode.get("gridId").asText();

            EnvEventGrid envEventGrid = new EnvEventGrid();
            envEventGrid.setEnvEventId(envEventId.intValue());
            envEventGrid.setGridId(gridId);
            envEventGrid.setEnabled(true);

            envEventGridRepository.save(envEventGrid);
        }
    }

    /* =====================================================
     * DSL 转换函数
     * ===================================================== */

    private EventFusionRule buildDSL(JsonNode flowJson) {

        // ---------- 1. id -> node ----------
        Map<String, JsonNode> nodeMap = new HashMap<>();
        for (JsonNode node : flowJson) {
            if (node.has("id")) {
                nodeMap.put(node.get("id").asText(), node);
            }
        }

        // ---------- 2. 分类 ----------
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
        String ruleName = publishNode.get("spaceEventName").asText()
                .replace("事件", "") + "规则";

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
            } else if ("spaceEvent".equals(eventSourceType)) {
                trigger.put("eventId", es.get("spaceEventType").asText());
            }
            triggers.add(trigger);
        }
        dsl.put("triggers", triggers);

        // ---------- 5. steps ----------
        List<Map<String, Object>> steps = new ArrayList<>();

        for (JsonNode es : eventSources) {
            JsonNode wires = es.get("wires");
            if (wires == null || wires.isEmpty()) continue;

            String nextId = wires.get(0).get(0).asText();

            while (nextId != null) {
                JsonNode opNode = nodeMap.get(nextId);
                if (opNode == null || !"Operator".equals(opNode.get("type").asText())) break;

                Map<String, Object> step = new LinkedHashMap<>();
                step.put("stepId", opNode.get("id").asText());

                String mode = opNode.get("mode").asText();
                step.put("operatorType", mode);

                if ("service".equals(mode)) {
                    step.put("operatorUrl", opNode.get("operatorURL").asText());
                    step.put("operatorHttpMethod", "POST");

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
                }

                // ---------- common / count ----------
                if ("common".equals(mode)) {
                    String operatorName = opNode.get("operator").asText();
                    step.put("operatorName", operatorName);

                    if ("count".equals(operatorName)) {
                        handleCountOperator(opNode, step);
                    }
                }

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
        publish.put("spaceEventId", publishNode.get("spaceEventType").asText());
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

        try {
            EventFusionRule fusionRule = objectMapper.convertValue(dsl, EventFusionRule.class);
            eventFusionRuleService.checkRuleValidity(fusionRule);
            return fusionRule;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* =====================================================
     * count 算子专用转换
     * ===================================================== */
    private void handleCountOperator(JsonNode opNode, Map<String, Object> step) {
        JsonNode value = opNode.get("value");

        List<Map<String, Object>> input = new ArrayList<>();

        // timeWindowMinute
        input.add(Map.of(
                "key", "timeWindowMinute",
                "type", "Number",
                "desc", "时间窗口（分钟），只统计该时间段内(N分钟前～现在)的事件数量。",
                "expr", value.get("timeWindowMinute").asText()
        ));

        // spaceEventId
        String eventId = value.get("countingEvent").asText();
        input.add(Map.of(
                "key", "spaceEventId",
                "type", "String",
                "desc", "环境事件ID，只统计该环境事件的数量。",
                "expr", "'" + eventId + "'"
        ));

        // conditions
        List<String> condExprs = new ArrayList<>();
        for (JsonNode c : value.get("conditions")) {
            String op = switch (c.get("operation").asText()) {
                case "Equal To" -> "EQ";
                case "Not Equal To" -> "NE";
                case "Greater Than" -> "GT";
                case "Greater Than or Equal To" -> "GTE";
                case "Less Than" -> "LT";
                case "Less Than or Equal To" -> "LTE";
                case "Like" -> "LIKE";
                default -> throw new IllegalArgumentException("Unsupported op");
            };

            condExprs.add(String.format(
                    "{'jsonPath': '%s', 'type': '%s', 'op': '%s', 'value': %s}",
                    c.get("jsonPath").asText(),
                    c.get("type").asText(),
                    op,
                    c.get("value").asText()
            ));
        }

        input.add(Map.of(
                "key", "countConditions",
                "type", "Array",
                "desc", "计数条件，指定对事件负载数据的过滤条件，条件间为AND关系，格式为 List<CountCondition>。",
                "expr", "{{" + String.join(", ", condExprs) + "}}"
        ));

        step.put("input", input);
        step.put("output", List.of(
                Map.of(
                        "key", "count",
                        "type", "Number",
                        "desc", "在指定时间窗口内，符合条件的环境事件数量。"
                )
        ));
    }

    /* =====================================================
     * eventJson 转换
     * ===================================================== */
    private String buildEventJson(JsonNode publishNode) {

        Map<String, Object> eventJson = new LinkedHashMap<>();
        eventJson.put("event_type", publishNode.get("spaceEventType").asText());
        eventJson.put("description", publishNode.get("spaceEventDesc").asText());

        Map<String, Object> params = new LinkedHashMap<>();
        for (JsonNode out : publishNode.get("outputsMapping")) {
            params.put(
                    out.get("key").asText(),
                    Map.of(
                            "type", adaptType(out.get("type").asText()),
                            "description", out.get("description").asText()
                    )
            );
        }

        eventJson.put("event_params", params);

        try {
            return objectMapper.writeValueAsString(eventJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String adaptType(String rawType) {
        if (rawType == null) {
            return null;
        }
        if ("Boolean".equals(rawType)) {
            return "bool";
        }
        return rawType.toLowerCase();
    }
}