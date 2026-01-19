package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <h3>EventIngestor 事件采集器</h3>
 * 负责从不同事件源（拉取或推送）获取事件，并发布为 Spring 事件进入流水线。
 * <p>
 * 不同事件源可通过继承该抽象类实现各自的采集协议与策略。
 * @author Lin Yicheng
 * @since 2026-01-16
 */
@RequiredArgsConstructor
public abstract class EventIngestor {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 获取采集器来源标识
     *
     * @return 来源标识
     */
    public abstract String getSourceId();

    /**
     * 发布采集到的事件<br/>
     * 实现类在完成事件采集后应调用该方法将事件发布到流水线中。
     *
     * @param eventBatch 事件列表
     */
    protected void publish(@NotNull EventBatch eventBatch) {
        if (eventBatch.events.isEmpty()) return;
        applicationEventPublisher.publishEvent(eventBatch);
    }

    public record EventBatch(List<BaseEvent> events) {}

    /**
     * <h3>DirectPushIngestor 直接推送采集器</h3>
     * 提供内部直接推送事件的入口。
     */
    @Component
    public static class DirectPushIngestor extends EventIngestor {

        @Autowired
        public DirectPushIngestor(ApplicationEventPublisher applicationEventPublisher) {
            super(applicationEventPublisher);
        }

        @Override
        public String getSourceId() {
            return "INNER_DIRECT_PUSH";
        }

        /**
         * 直接推送事件
         *
         * @param event 待发布事件
         */
        public void push(@NotNull BaseEvent event) {
            publish(new EventBatch(List.of(event)));
        }
    }

}
