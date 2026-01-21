package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.DataEvent;
import edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan.EventFusionRuleEntity;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.EventFusionRuleService;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component.EventIngestor;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fusion-rules")
@Tag(name = "EventFusionRuleController", description = "事件融合规则相关接口")
@RequiredArgsConstructor
class EventFusionRuleController {

    private final EventFusionRuleService eventFusionRuleService;
    private final EventIngestor.DirectPushIngestor directPushIngestor;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Tag(name = "创建事件融合规则")
    public EventFusionRuleEntity createFusionRule(@Valid @RequestBody EventFusionRule eventFusionRule) {
        return eventFusionRuleService.createOrUpdateRule(null, eventFusionRule);
    }

    @PutMapping("/{id}")
    @Tag(name = "更新事件融合规则")
    public EventFusionRuleEntity updateFusionRule(@PathVariable Integer id, @Valid @RequestBody EventFusionRule eventFusionRule) {
        return eventFusionRuleService.createOrUpdateRule(id, eventFusionRule);
    }

    @PostMapping("/event")
    @Tag(name = "模拟事件发生并推送事件到规则引擎流水线")
    public void pushEventToPipeline(@RequestBody DataEvent eventData) {
        directPushIngestor.push(eventData);
    }
}
