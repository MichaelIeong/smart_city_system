package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.DataEvent;
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

    // TODO: 模拟不同的发布通道，等待具体实现
    @Component
    public static class Channel1Publisher extends EventPublisher {
        @Override
        public void publish(DataEvent result) {
            System.out.println("[Channel1] Publishing event: " + result.getIdentifier() + " By " + Thread.currentThread().getName());
        }
    }

    // TODO: 模拟不同的发布通道，等待具体实现
    @Component
    public static class Channel2Publisher extends EventPublisher {
        @Override
        public void publish(DataEvent result) {
            System.out.println("[Channel2] Publishing event: " + result.getIdentifier() + " By " + Thread.currentThread().getName());
        }
    }
}
