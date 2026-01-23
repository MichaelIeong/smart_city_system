package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.DataEvent;
import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <h3>RuleMatcher 规则匹配器</h3>
 * 根据事件融合规则定义，匹配能够触发该规则的事件组，输出[触发事件组, 规则]的匹配结果。
 * @author Lin Yicheng
 * @since 2026-01-16
 */
@RequiredArgsConstructor
@Component
public class RuleMatcher {

    private final EnvEventRepository envEventRepository;

    /**
     * MatchResult 规则匹配结果
     * @param triggers 触发该规则的事件列表
     * @param rule 被触发的规则
     */
    public record MatchResult(
        List<DataEvent> triggers,
        EventFusionRule rule
    ) {}

    /**
     * 规则匹配
     * <p>
     * 当前仅支持单触发源规则：将所有事件组展平为单个事件列表，逐条匹配规则的触发器。
     * 若事件的 eventSource 与 eventId 同时匹配，则视为触发该规则。
     *
     * @param groupedEvents 已分组的事件列表
     * @return 规则匹配结果列表
     */
    public List<MatchResult> match(List<List<DataEvent>> groupedEvents) {
        var rulesWithSingleTrigger = envEventRepository
            .findAll()
            .stream()
            .map(EnvEvent::getRuleDsl)
            .filter(Objects::nonNull)
            .filter(rule -> rule.triggers().size() == 1)
            .toList();

        var allEvents = groupedEvents.stream().flatMap(List::stream).toList();

        List<MatchResult> results = new ArrayList<>();
        for (var rule : rulesWithSingleTrigger) {
            for (var event : allEvents) {
                var trigger = rule.triggers().get(0);
                if (Objects.equals(event.getEventSource(), trigger.eventSource()) &&
                    Objects.equals(event.getEventId(), trigger.eventId())) {
                    results.add(new MatchResult(List.of(event), rule));
                }
            }
        }

        return results;
    }
}
