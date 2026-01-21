package edu.fudan.se.sctap_lowcode_tool.config;

import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.EventFusionPipeline;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * <h3>EventFusionPipelineConfig 事件融合流水线配置</h3>
 * 负责组装 EventFusionPipeline 及其依赖组件，并注入默认实现。
 * @author Lin Yicheng
 * @since 2026-01-16
 */
@Configuration
public class EventFusionPipelineConfig {

    /**
     * 构建默认事件融合流水线
     *
    * @param eventCollector 事件收集器，使用 ImmediateEventCollector：收到事件立即触发，不做缓存。
    * @param eventGrouper 事件分组器，使用 OneEventPerGroup：每个事件独立成组。
    * @param ruleMatcher 规则匹配器，默认实现，按触发条件匹配规则。
    * @param ruleExecutor 规则执行器，默认实现。
    * @param eventPublishers 事件发布器列表，注入所有已注册的实现。
    * @param matcherPool 规则匹配线程池。
    * @param executorPool 规则执行线程池。
     * @return 组装完成的事件融合流水线
     */
    @Bean
    public EventFusionPipeline defaultEventFusionPipeline(
            @Qualifier("eventCollector.ImmediateEventCollector") EventCollector eventCollector,
            @Qualifier("eventGrouper.OneEventPerGroup") EventGrouper eventGrouper,
            RuleMatcher ruleMatcher,
            RuleExecutor ruleExecutor,
            List<EventPublisher> eventPublishers,
            @Qualifier("fusionEngineMatcher") Executor matcherPool,
            @Qualifier("fusionEngineExecutor") Executor executorPool
    ) {
        return new EventFusionPipeline(
                eventCollector, eventGrouper,
                ruleMatcher, ruleExecutor, eventPublishers,
                matcherPool, executorPool
        );
    }
}
