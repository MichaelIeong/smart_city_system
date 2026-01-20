package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.DataEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule.EventSource.sensorEvent;
import static edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule.EventSource.spaceEvent;

/**
 * <h3>RuleExecutor 规则执行器</h3>
 * 负责执行匹配到的规则，并生成融合后的事件结果。
 * @author Lin Yicheng
 * @since 2026-01-16
 */
@Component
public class RuleExecutor {
    /**
     * 执行规则
     *
     * @param rule 待执行的规则
     * @param triggers 触发该规则的事件列表
     * @return 执行结果事件，若该规则未生成融合结果则返回空 Optional
     */
    public Optional<DataEvent> execute(EventFusionRule rule, List<DataEvent> triggers) {
        return mockExecute(rule, triggers);
    }

    // TODO: 模拟规则执行，实际应调用规则引擎
    @NotNull
    private static Optional<DataEvent> mockExecute(EventFusionRule rule, List<DataEvent> triggers) {
        System.out.println("Executing(" + triggers.get(0).getIdentifier() + ") By " + Thread.currentThread().getName());
        System.out.println(triggers.get(0).getPayload());
        try {
            Thread.sleep(1000); // Simulate execution time
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (triggers.get(0).getEventSource() == sensorEvent) {
            var result = DataEvent.builder()
                                  .timestamp(System.currentTimeMillis())
                                  .sourceIngestor("EventFusionEngine")
                                  .eventSource(spaceEvent)
                                  .identifier("fused-" + triggers.get(0).getIdentifier())
                                  .eventId("truck_spill")
                                  .payload(Map.of("key", "value"))
                                  .build();
            return Optional.of(result);
        } else {
            return Optional.empty();
        }

    }
}
