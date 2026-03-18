package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.DataEvent;
import edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan.EventFusionRunHistory;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.EventFusionRunHistoryService;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component.EventIngestor;
import edu.fudan.se.sctap_lowcode_tool.utils.TslApiUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/fusion-rules")
@Tag(name = "EventFusionRuleController", description = "事件融合规则相关接口")
@RequiredArgsConstructor
class EventFusionRuleController {

    private final EventIngestor.DirectPushIngestor directPushIngestor;
    private final EventFusionRunHistoryService historyService;
    private final TslApiUtil tslApiUtil;
    private final String TSL_EVENT_PUSH_ENDPOINT = "http://60.161.136.138:32014/metrics/eventCenter/add";

    @PostMapping("/event")
    @Tag(name = "模拟事件发生并推送事件到规则引擎流水线")
    public void pushEventToPipeline(@RequestBody DataEvent eventData) {
        directPushIngestor.push(eventData);
        if (eventData.getEventSource() == EventFusionRule.EventSource.spaceEvent) {
            return;
        }
        // 如果模拟SensorEvent发生，则向特斯联推送该事件
        try {
            tslApiUtil.fetch(
                restClient -> restClient
                    .post()
                    .uri(TSL_EVENT_PUSH_ENDPOINT)
                    .headers(tslApiUtil.buildHeaders(null))
                    .body(eventData.getPayload())
                    .retrieve(),
                10, new TypeReference<String>() {}
            );
        } catch (TslApiUtil.TslApiException e) {
            log.warn("Mock事件发生后试图向特斯联推送该事件时发生异常: {}", e.getMessage());
        }
    }

    @GetMapping("/histories")
    @Tag(name = "获取事件融合规则运行历史")
    public PageDTO<EventFusionRunHistoryService.BriefResponse> getFusionRuleHistories(
        @RequestParam(defaultValue = "0") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        return historyService.getRunHistories(pageNum, pageSize);
    }

    @GetMapping("/histories/{id}")
    @Tag(name = "获取事件融合规则运行历史详情")
    public EventFusionRunHistory getFusionRuleHistoryDetail(@PathVariable Integer id) throws BadRequestException {
        return historyService.getRunHistoryDetail(id);
    }
}
