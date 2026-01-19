package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.BaseEvent;
import lombok.Synchronized;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

/**
 * <h3>EventCollector 事件收集器</h3>
 * 负责接收并缓存来自流水线入口的事件，按照策略判断是否需要批量 flush。
 * @author Lin Yicheng
 * @since 2026-01-16
 */
public abstract class EventCollector {

    private final BlockingQueue<BaseEvent> buffer = new LinkedBlockingQueue<>();

    /**
     * 收集单条事件<br/>
     * 调用该方法将事件放入缓存区。
     *
     * @param event 待收集事件
     * @throws RejectedExecutionException 缓冲区已满时抛出
     */
    public void collect(@NotNull BaseEvent event) throws RejectedExecutionException{
        if (!buffer.offer(event)) {
            throw new RejectedExecutionException("EventCollector 缓冲区已满，无法接收新事件");
        }
        onEventCollected(event);
    }

    /**
     * 判断是否达到排空条件
     * <p>
     * 实现类在实现时应考虑线程安全问题。
     *
     * @return true 表示应触发 drain
     */
    public abstract boolean shouldDrain();

    /**
     * 批量取出缓存事件并重置策略状态
     *
     * @return 缓冲区内的所有事件列表
     */
    @Synchronized
    public List<BaseEvent> drain() {
        if (buffer.isEmpty()) return new ArrayList<>();
        ArrayList<BaseEvent> result = new ArrayList<>();
        buffer.drainTo(result);
        resetPolicy();
        return result;
    }

    /**
     * Collector 在收到新事件后会自动调用该方法
     * <p>
     * 实现类应自行维护状态变量，以便在 shouldDrain 中使用。<br/>
     * 实现类在实现时应考虑线程安全问题。
     *
     * @param event 新收集到的事件
     */
    protected abstract void onEventCollected(BaseEvent event);

    /**
     * Collector 在完成 drain 后会自动调用该方法
     * <p>
     * 实现类应重置其维护的状态变量。
     */
    protected abstract void resetPolicy();


    /**
     * <h3>ImmediateEventCollector 立即触发收集器</h3>
     * 收到事件立即触发 drain，不做缓存策略控制。
     */
    @Component
    public static class ImmediateEventCollector extends EventCollector {
        @Override public boolean shouldDrain() {return true;}
        @Override protected void onEventCollected(BaseEvent event) {}
        @Override protected void resetPolicy() {}
    }
}
