package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component;

import edu.fudan.se.sctap_lowcode_tool.DTO.EventTriggerRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.DataEvent;
import edu.fudan.se.sctap_lowcode_tool.controller.AppRuleExecutorController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
     * <h3>LocalDirectPushChannel (本地部署)直接推送通道</h3>
     * 将融合结果直接回注到本地的流水线入口（内部推送）。
     */
    @Component
    @ConditionalOnProperty(name = "app.deploy-mode", havingValue = "local")
    @RequiredArgsConstructor
    public static class LocalDirectPushChannel extends EventPublisher {
        private final EventIngestor.DirectPushIngestor directPushIngestor;
        @Override public void publish(DataEvent result) {directPushIngestor.push(result);}
    }

    /**
     * <h3>LocalAppRuleChannel (本地部署)应用规则通道</h3>
     * 将融合结果转为应用规则触发请求，并向本地 AppRuleExecutorController 触发。
     */
    @Component
    @ConditionalOnProperty(name = "app.deploy-mode", havingValue = "local")
    @RequiredArgsConstructor
    public static class LocalAppRuleChannel extends EventPublisher {
        private final AppRuleExecutorController controller;
        @Override public void publish(DataEvent result) {
            var request = new EventTriggerRequest();
            request.setEvent_type(result.getEventId());
            request.setEvent_params(result.getPayload());
            controller.triggerAppRule(request);
        }
    }

    /**
     * <h3>DistributedDirectPushChannel (分布式部署)远端直接推送通道</h3>
     * 将融合结果通过 HTTP 推送到云端 {@code EventFusionRuleController} 的 {@code POST /api/fusion-rules/event}。
     */
    @Component
    @ConditionalOnProperty(name = "app.deploy-mode", havingValue = "distributed")
    @Slf4j
    public static class DistributedDirectPushChannel extends EventPublisher {

        private static final String FUSION_EVENT_PATH = "/api/fusion-rules/event";

        private final RestTemplate restTemplate;
        private final String cloudBaseUrl;

        public DistributedDirectPushChannel(
            RestTemplate restTemplate,
            @Value("${app.cloud-url:}") String cloudBaseUrl
        ) {
            this.restTemplate = restTemplate;
            this.cloudBaseUrl = cloudBaseUrl == null ? "" : cloudBaseUrl.trim();
        }

        @Override
        public void publish(DataEvent result) {
            if (cloudBaseUrl.isEmpty()) {
                log.warn("【分布式模式】app.cloud-url 未配置，跳过向云端推送融合事件");
                return;
            }
            String url = cloudBaseUrl + FUSION_EVENT_PATH;
            try {
                restTemplate.postForEntity(url, result, Void.class);
            } catch (Exception e) {
                log.error("【分布式模式】向云端推送融合事件失败 [{}]: {}", url, e.getMessage());
            }
        }
    }

    /**
     * <h3>DistributedAppRuleChannel (分布式部署)远端应用规则通道</h3>
     * 将融合结果转为应用规则触发请求，并 HTTP 调用云端 {@code AppRuleExecutorController} 的 {@code POST /api/tapExecutor/trigger}。
     */
    @Component
    @ConditionalOnProperty(name = "app.deploy-mode", havingValue = "distributed")
    @Slf4j
    public static class DistributedAppRuleChannel extends EventPublisher {

        private static final String TRIGGER_APP_RULE_PATH = "/api/tapExecutor/trigger";

        private final RestTemplate restTemplate;
        private final String cloudBaseUrl;

        public DistributedAppRuleChannel(
            RestTemplate restTemplate,
            @Value("${app.cloud-url:}") String cloudBaseUrl
        ) {
            this.restTemplate = restTemplate;
            this.cloudBaseUrl = cloudBaseUrl == null ? "" : cloudBaseUrl.trim();
        }

        @Override
        public void publish(DataEvent result) {
            if (cloudBaseUrl.isEmpty()) {
                log.warn("【分布式模式】app.cloud-url 未配置，跳过向云端触发应用规则");
                return;
            }
            var request = new EventTriggerRequest();
            request.setEvent_type(result.getEventId());
            request.setEvent_params(result.getPayload());
            String url = cloudBaseUrl + TRIGGER_APP_RULE_PATH;
            try {
                restTemplate.postForEntity(url, request, Void.class);
            } catch (Exception e) {
                log.error("【分布式模式】向云端触发应用规则失败 [{}]: {}", url, e.getMessage());
            }
        }
    }

}
