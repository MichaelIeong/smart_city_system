package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.SensorTypeDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule;
import edu.fudan.se.sctap_lowcode_tool.constant.RoleConstant;
import edu.fudan.se.sctap_lowcode_tool.model.*;
import edu.fudan.se.sctap_lowcode_tool.repository.*;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.EventFusionRuleService;
import edu.fudan.se.sctap_lowcode_tool.DTO.ProductEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FusionNodeRedService {

    private final TslDeviceRepository deviceRepository;
    private final EnvEventRepository envEventRepository;
    private final EnvEventGridRepository envEventGridRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventFusionRuleService eventFusionRuleService;
    private final ProductEventRepository productEventRepository;

    @Value("${app.node-role:edge}")
    private String nodeRole;

    @Autowired
    private EdgeNodeRepository edgeNodeRepository;

    @Autowired
    private RestTemplate restTemplate;

    public FusionNodeRedService(
        TslDeviceRepository deviceRepository,
        EnvEventRepository envEventRepository,
        EnvEventGridRepository envEventGridRepository,
        EventFusionRuleService eventFusionRuleService,
        ProductEventRepository productEventRepository
    ) {
        this.deviceRepository = deviceRepository;
        this.envEventRepository = envEventRepository;
        this.envEventGridRepository = envEventGridRepository;
        this.eventFusionRuleService = eventFusionRuleService;
        this.productEventRepository = productEventRepository;
    }

    /* =====================================================
     * Sensor Event（设备事件）
     * ===================================================== */

    public List<SensorTypeDTO> listSensorTypesInGrid(String gridId) {

        List<TslProduct> products =
                deviceRepository.findDistinctProductsByMeshId(gridId);

        return products.stream()
                .map(p -> new SensorTypeDTO(
                        p.getProductId(),
                        p.getProductName()
                ))
                .collect(Collectors.toList());
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
    * SpaceEvent Payload
    * ===================================================== */
    public List<String> getPayloadByEventType(String eventType) {

        List<EnvEvent> events = envEventRepository.findByEventType(eventType);

        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }

        EnvEvent event = events.get(0);

        try {
            JsonNode root = objectMapper.readTree(event.getEventJson());

            JsonNode paramsNode = root.path("event_params");

            if (paramsNode.isMissingNode() || !paramsNode.isObject()) {
                return Collections.emptyList();
            }

            List<String> payload = new ArrayList<>();

            paramsNode.fieldNames().forEachRemaining(payload::add);

            return payload;

        } catch (Exception e) {
            log.error("解析 eventJson 失败: {}", e.getMessage());
            return Collections.emptyList();
        }
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

    public void handleUploadRule(JsonNode flowJson, Integer id) throws JsonProcessingException {
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
        // 如果是边端节点，需要制定为云端的id
        if(RoleConstant.CLOUD.equals(nodeRole)) {
            envEvent = envEventRepository.save(envEvent);
        } else {
            envEvent.setId(id);
            String ruleDslStr = envEvent.getRuleDsl() != null ? objectMapper.writeValueAsString(envEvent.getRuleDsl()) : null;
            String dependDtypesStr = envEvent.getDependDtypes() != null ? objectMapper.writeValueAsString(envEvent.getDependDtypes()) : null;
            envEventRepository.insertWithId(envEvent, ruleDslStr, dependDtypesStr);
        }
        Integer envEventId = envEvent.getId();

        // ---------- 若非跨网格，组装并入库 EnvEventGrid ----------
        String gridId = eventSourceNode.get("gridId").asText();
        if (!crossRegion) {

            EnvEventGrid envEventGrid = new EnvEventGrid();
            envEventGrid.setEnvEventId(envEventId);
            envEventGrid.setGridId(gridId);
            envEventGrid.setEnabled(true);

            envEventGridRepository.save(envEventGrid);
        }

        // ================= 新增：云端下发逻辑 =================
        if (RoleConstant.CLOUD.equalsIgnoreCase(nodeRole)) {
            if (crossRegion) {
                // 根据您的需求，如果是跨区域则不需要下发
                log.info("当前规则为 crossRegion，无需下发至边缘节点");
            } else if (gridId != null) {
                // 非跨区域，按 gridId 查找并下发
                dispatchRuleToEdge(flowJson, gridId, envEventId);
            }
        }
    }

    /**
     * 将规则下发至指定的边缘节点
     */
    private void dispatchRuleToEdge(JsonNode flowJson, String gridId, Integer cloudGeneratedId) {
        EdgeNode targetNode = edgeNodeRepository.findByGridId(gridId);
        if (targetNode == null) {
            log.warn("未找到 gridId = {} 对应的边缘节点，跳过规则下发。", gridId);
            return;
        }
        String ipAddress = targetNode.getIpAddress();
        // 拼接目标边端的 URL，加上可选的 id 参数
        String url = ipAddress + "/api/node-red/fusion/uploadRule?id={id}";
        try {
            // 使用 postForEntity 将 flowJson 发送过去
            restTemplate.postForEntity(
                    url,
                    flowJson,
                    Map.class,
                    cloudGeneratedId // 对应 URL 中的 {id}
            );
            log.info("边缘节点 [{}] 事件融合下发成功", ipAddress);
        } catch (Exception e) {
            log.error("向边缘节点 [{}] 事件融合下发失败: {}", ipAddress, e.getMessage());
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

        JsonNode eventSourceNode = eventSources.get(0);
        String eventSourceType = eventSourceNode.get("eventSourceType").asText();

        String eventId;
        if ("sensorEvent".equals(eventSourceType)) {
            eventId = eventSourceNode.get("sensingEvent").asText();
        } else if ("spaceEvent".equals(eventSourceType)) {
            eventId = eventSourceNode.get("spaceEventType").asText();
        } else {
            throw new IllegalArgumentException("Unsupported eventSourceType: " + eventSourceType);
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
            String est = es.get("eventSourceType").asText();
            trigger.put("eventSource", est);

            if ("sensorEvent".equals(est)) {
                trigger.put("eventId", es.get("sensingEvent").asText());
            } else if ("spaceEvent".equals(est)) {
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

                        String source = input.get("source").asText();
                        in.put("expr", buildTriggerExpr(eventSourceType, eventId, source));

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

                    if ("Count".equals(operatorName)) {
                        handleCountOperator(opNode, step, eventSourceType, eventId);
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

        JsonNode conditionNode = publishNode.get("publishCondition");

        String level1 = conditionNode.get("level1").asText();
        String level2 = conditionNode.get("level2").asText();
        String expression = conditionNode.get("expression").asText();

        String conditionExpr;
        if ("eventsource".equalsIgnoreCase(level1)) {
            conditionExpr = String.format(
                    "#triggers['%s']['%s']['%s']%s",
                    eventSourceType, eventId, level2, expression == null ? "" : expression
            );
        } else {
            String operatorId = findLastOperatorId(steps);
            conditionExpr = String.format(
                    "#stepOutputs['%s']['%s']%s",
                    operatorId, level2, expression == null ? "" : expression
            );
        }

        publish.put("condition", conditionExpr);

        // outputsMapping
        List<Map<String, Object>> publishOutputs = new ArrayList<>();
        for (JsonNode out : publishNode.get("outputsMapping")) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("key", out.get("key").asText());
            o.put("type", out.get("type").asText());
            o.put("desc", out.get("description").asText());

            String source = out.get("source").asText();
            o.put("expr", buildTriggerExpr(eventSourceType, eventId, source));

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
    private void handleCountOperator(JsonNode opNode,
                                     Map<String, Object> step,
                                     String eventSourceType,
                                     String eventId) {

        JsonNode value = opNode.get("value");

        List<Map<String, Object>> input = new ArrayList<>();

        int timeWindowSeconds = value.get("timeWindowMinute").asInt() * 60;

        input.add(Map.of(
                "key", "timeWindowSeconds",
                "type", "Number",
                "desc", "时间窗口（秒），只统计该时间段内(N秒前～现在)的事件数量。",
                "expr", String.valueOf(timeWindowSeconds)
        ));

        String countingEventId = value.get("countingEvent").asText();
        input.add(Map.of(
                "key", "spaceEventId",
                "type", "String",
                "desc", "环境事件ID，只统计该环境事件的数量。",
                "expr", "'" + countingEventId + "'"
        ));

        // conditions
        List<String> condExprs = new ArrayList<>();
        for (JsonNode c : value.path("conditions")) {
            String op = switch (c.path("operation").asText()) {
                case "Equal To" -> "EQ";
                case "Not Equal To" -> "NE";
                case "Greater Than" -> "GT";
                case "Greater Than or Equal To" -> "GTE";
                case "Less Than" -> "LT";
                case "Less Than or Equal To" -> "LTE";
                case "Like" -> "LIKE";
                default -> throw new IllegalArgumentException(
                        "Unsupported op: " + c.path("operation").asText()
                );
            };

            String jsonPath = "$." + c.path("jsonPath").asText();
            String valueExpr = buildTriggerExpr(eventSourceType, eventId, c.path("value").asText());

            condExprs.add(String.format(
                    "{'jsonPath': '%s', 'type': '%s', 'op': '%s', 'value': %s}",
                    jsonPath,
                    c.path("type").asText(),
                    op,
                    valueExpr
            ));
        }

        input.add(Map.of(
                "key", "countConditions",
                "type", "Array",
                "desc", "计数条件",
                "expr", "{" + String.join(", ", condExprs) + "}"
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

    private String buildTriggerExpr(String eventSourceType, String eventId, String field) {
        return String.format("#triggers['%s']['%s']['%s']",
                eventSourceType, eventId, field);
    }

    private String findLastOperatorId(List<Map<String, Object>> steps) {
        return steps.get(steps.size() - 1).get("stepId").toString();
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