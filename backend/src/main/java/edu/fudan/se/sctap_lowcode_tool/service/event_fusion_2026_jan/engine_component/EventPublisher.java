package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component;

import edu.fudan.se.sctap_lowcode_tool.DTO.EventTriggerRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.DataEvent;
import edu.fudan.se.sctap_lowcode_tool.controller.AppRuleExecutorController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * <h3>EventPublisher 事件发布器</h3>
 * 负责将事件融合执行引擎的结果发布到不同的下游通道。
 * @author Lin Yicheng
 * @since 2026-01-16
 */
public abstract class EventPublisher {
    /**
     * 发布融合后的事件
     *
     * @param result 事件融合执行引擎的结果事件
     */
    public abstract void publish(DataEvent result);

    /**
     * <h3>DirectPushChannel 直接推送通道</h3>
     * 将融合结果直接回注到流水线入口（内部推送）。
     */
    @Component
    @RequiredArgsConstructor
    public static class DirectPushChannel extends EventPublisher {
        private final EventIngestor.DirectPushIngestor directPushIngestor;
        @Override public void publish(DataEvent result) {directPushIngestor.push(result);}
    }

    /**
     * <h3>AppRuleChannel 应用规则通道</h3>
     * 将融合结果转为应用规则触发请求。
     */
    @Component
    @RequiredArgsConstructor
    public static class AppRuleChannel extends EventPublisher {
        private final AppRuleExecutorController controller;
        @Override public void publish(DataEvent result) {
            var request = new EventTriggerRequest();
            request.setEvent_type(result.getEventId());
            request.setEvent_params(result.getPayload());
            controller.triggerAppRule(request);
        }
    }
}
