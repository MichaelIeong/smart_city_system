package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.BaseEvent;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component.*;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component.EventIngestor.EventBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * <h3>EventFusionPipeline 事件融合流水线</h3>
 * 负责将事件收集、分组、规则匹配、规则执行与发布串联成异步处理流程。
 * <p>
 * 处理链路：事件收集 → 事件分组 → 规则匹配 → 规则执行 → 事件发布。
 * @author Lin Yicheng
 * @since 2026-01-16
 */
@RequiredArgsConstructor
@Slf4j
public class EventFusionPipeline {

    // =====================================================================
    // 定时触发功能未实现
    // 若要改为定时触发，可新增 enableTimer 字段，并修改 onReceiveEvents 实现
    // =====================================================================

    private final EventCollector eventCollector;
    private final EventGrouper eventGrouper;
    private final RuleMatcher ruleMatcher;
    private final RuleExecutor ruleExecutor;
    private final List<EventPublisher> eventPublishers;
    private final Executor matcherPool;
    private final Executor executorPool;

    /**
     * 监听事件并触发融合流水线处理
     * <p>
     * 执行流程：
     * <ol>
     *   <li>[Collector] 将事件交给收集器缓存</li>
     *   <li>[Collector] 达到阈值后触发 drain</li>
     *   <li>[Grouper, Matcher] 异步进行分组与规则匹配</li>
     *   <li>[Executor, Publisher] 异步执行规则并发布融合事件</li>
     * </ol>
     *
     * @param eventBatch 事件列表（来自 EventIngestor 发布的 Spring 事件）
     */
    @EventListener
    public void onReceiveEvents(EventBatch eventBatch) {
        // 收集并缓存事件
        eventBatch.events().forEach(event -> {
            try {
                eventCollector.collect(event);
            } catch (RejectedExecutionException e) {
                log.warn("{}, 丢弃事件: {}", e.getMessage(), event);
            }
        });
        boolean shouldDrain = eventCollector.shouldDrain();
        if (!shouldDrain) return;
        // 缓存事件达到阈值
        var drainResult = eventCollector.drain();
        if (drainResult.isEmpty()) return;
        CompletableFuture
            // 使用 matcher 线程池异步执行分组和匹配
            .supplyAsync(() -> groupAndMatch(drainResult), matcherPool)
            // 使用 executor 线程池异步执行规则
            .thenAccept(this::executeAndPublish)
            // 异常处理
            .exceptionally(ex -> {
                log.error("事件融合处理过程中发生了未捕获的流水线异常: ", ex);
                return null;
            });
    }

    /**
     * 分组并匹配规则
     *
     * @param events 待分组事件列表
     * @return 匹配结果列表，包含规则与对应触发事件
     */
    private List<RuleMatcher.MatchResult> groupAndMatch(List<BaseEvent> events) {
        var groupedEvents = eventGrouper.apply(events);
        return ruleMatcher.match(groupedEvents);
    }

    /**
     * 执行规则并发布融合事件
     *
     * @param matchResults 规则匹配结果列表
     */
    private void executeAndPublish(List<RuleMatcher.MatchResult> matchResults) {
        matchResults.forEach(match -> CompletableFuture
            .supplyAsync(() -> ruleExecutor.execute(match.rule(), match.triggers()), executorPool)
            .thenAccept(optionalResult -> optionalResult.ifPresent(
                result -> {
                    for (EventPublisher publisher : eventPublishers) {
                        publisher.publish(result);
                    }
                })
            )
            .exceptionally(ex -> {
                log.error("[事件融合] 规则执行/事件发布过程中发生了未捕获的异常: ", ex);
                log.error("规则: \n{}\n触发事件: \n{}", match.rule(), match.triggers());
                return null;
            })
        );
    }

}
