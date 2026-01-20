package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.BaseEvent;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.DataEvent;
import edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan.SpaceEventHistory;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceEventHistoryRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.TslApiUtil;
import edu.fudan.se.sctap_lowcode_tool.utils.TslApiUtil.TslApiException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.sqm.sql.ConversionException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule.EventSource.sensorEvent;
import static edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule.EventSource.spaceEvent;

/**
 * <h3>EventIngestor 事件采集器</h3>
 * 负责从不同事件源（拉取或推送）获取事件，并发布为 Spring 事件进入流水线。
 * <p>
 * 不同事件源可通过继承该抽象类实现各自的采集协议与策略。
 *
 * @author Lin Yicheng
 * @since 2026-01-16
 */
@Slf4j
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

    public record EventBatch(List<? extends BaseEvent> events) {}

    /**
     * <h3>DirectPushIngestor 直接推送采集器</h3>
     * 提供内部直接推送事件的入口，如果事件是 spaceEvent 类型，则存储到 SpaceEventHistory 中。
     */
    @Component
    public static class DirectPushIngestor extends EventIngestor {

        private final SpaceEventHistoryRepository spaceEventHistoryRepository;

        @Autowired
        public DirectPushIngestor(
            ApplicationEventPublisher applicationEventPublisher,
            SpaceEventHistoryRepository spaceEventHistoryRepository
        ) {
            super(applicationEventPublisher);
            this.spaceEventHistoryRepository = spaceEventHistoryRepository;
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
            // 如果事件是 DataEvent，且事件类型是 spaceEvent，则存储到 SpaceEventHistory 中
            if (event instanceof DataEvent dataEvent && dataEvent.getEventSource().equals(spaceEvent)) {
                var spaceEventHistory = new SpaceEventHistory();
                spaceEventHistory.setSpaceEventId(dataEvent.getEventId());
                spaceEventHistory.setPayload(dataEvent.getPayload());
                spaceEventHistoryRepository.save(spaceEventHistory);
            }
            // 发布事件
            publish(new EventBatch(List.of(event)));
        }
    }

    /**
     * <h3>TslScheduledFetcher 特斯联定时拉取采集器</h3>
     * 通过定时任务从特斯联数据源拉取事件并发布到流水线。
     */
    @Component
    public static class TslScheduledFetcher extends EventIngestor {

        private final TslApiUtil tslApiUtil;
        private final ObjectMapper objectMapper;
        private final ConversionService conversionService;

        @Value("${tsl.app.base-url}")
        private String tslBaseUrl;
        private static final String tslApiUrl = "/metrics/eventCenter/page";
        
        /**
         * 监听的网格列表，拉取事件时只保留这些网格的事件
         */
        @Value("${fusion.listen-network}")
        private String[] listenNetworks;

        /**
         * 接口请求超时时间（秒）
         */
        private static final int timeoutSec = 30;
        

        /**
         * 上次拉取到的最新事件的创建时间，下一次拉取会以此作为起始时间
         */
        @Getter @Setter
        private LocalDateTime lastFetchTime = LocalDateTime.now();

        @Autowired
        public TslScheduledFetcher(
            ApplicationEventPublisher applicationEventPublisher,
            TslApiUtil tslApiUtil,
            ObjectMapper objectMapper,
            ConversionService conversionService
        ) {
            super(applicationEventPublisher);
            this.tslApiUtil = tslApiUtil;
            this.objectMapper = objectMapper;
            this.conversionService = conversionService;
        }

        @Override
        public String getSourceId() {
            return "TSL_SCHEDULED_FETCHER";
        }

        /**
         * 定时拉取任务入口
         */
        @Async("tslScheduledFetcher")
        @Scheduled(fixedDelay = 10_000) // 每次执行完成后等待 10 秒进行下次执行
        public void scheduleFetch() {
            try {
                doFetch();
            } catch (Exception e) {
                log.error("[TslScheduledFetcher] 事件融合引擎(特斯联数据源) 在定时拉取的过程中发生异常: ", e);
            }
        }

        
        private void doFetch() throws TslApiException {
            // 根据上次拉取时间获取新事件
            var fetched = fetchEventsSince(lastFetchTime);

            // 获取结果中的最新时间作为下一次拉取的起始时间
            fetched.stream()
                   .map(e -> {
                       try {
                           Long createTimeMillis = conversionService.convert(e.get("createTime"), Long.class);
                           if (createTimeMillis == null) return null;
                           createTimeMillis += 1;
                           return LocalDateTime.ofInstant(
                               Instant.ofEpochMilli(createTimeMillis),
                               ZoneId.systemDefault()
                           );
                       } catch (Exception ex) {
                           return null;
                       }
                   })
                   .filter(Objects::nonNull)
                   .max(LocalDateTime::compareTo)
                   .ifPresent(newLastFetchTime -> this.lastFetchTime = newLastFetchTime);

            // 只保留当前订阅网格的事件，并将结果转换为 DataEvent 并发布
            var results = fetched
                .stream()
                .filter(e -> {
                    String network = String.valueOf(e.get("dutyNetwork"));
                    return e.get("dutyNetwork") != null && Arrays.asList(listenNetworks).contains(network);
                })
                .map(e -> DataEvent
                    .builder()
                    .timestamp(System.currentTimeMillis())
                    .sourceIngestor(this.getSourceId())
                    .eventSource(sensorEvent)
                    .identifier("tsl-" + e.getOrDefault("id", UUID.randomUUID()))
                    .eventId(String.valueOf(e.get("eventType")))
                    .payload(e)
                    .build()
                )
                .toList();

            publish(new EventBatch(results));
        }

        /**
         * 拉取指定时间之后的事件
         * <p>
         * 采用分页方式循环访问特斯联接口：先请求第一页获取 totalPages，再逐页拉取并累计结果。<br/>
         * 当结果超过 1000 条时会提前终止。
         *
         * @param startTime 起始时间（以该时间为增量拉取起点）
         * @return 原始事件列表（未做业务过滤）
         * @throws TslApiException 接口调用或响应解析失败时抛出
         */
        public List<Map<String, Object>> fetchEventsSince(
            @NotNull LocalDateTime startTime
        ) throws TslApiException {
            List<Map<String, Object>> results = new ArrayList<>();
            int pageSize = 50;
            int pageNum = 1;

            Map<String, Object> firstPage = postEndpoint(startTime, pageNum, pageSize);
            int totalPages = parseTotalPages(firstPage);
            addPage(results, firstPage);

            for (pageNum = 2; pageNum <= totalPages; pageNum++) {
                Map<String, Object> page = postEndpoint(startTime, pageNum, pageSize);
                addPage(results, page);
                if (results.size() > 1000) break;
            }
            return results;
        }

        /**
         * 解析响应中的总页数
         * <p>
         * 该方法从接口响应中读取 totalPages 字段，并转换为整数。
         * 若字段缺失、类型不合法或值为负数，将抛出异常以提示响应格式错误。
         *
         * @param response 接口响应
         * @return 总页数（应为非负整数）
         * @throws TslApiException 当 totalPages 缺失或无法解析时抛出
         */
        private int parseTotalPages(Map<String, Object> response) throws TslApiException {
            try {
                Integer totalPages = conversionService.convert(response.get("totalPages"), Integer.class);
                if (totalPages == null || totalPages < 0) throw new IllegalArgumentException();
                return totalPages;
            } catch (ConversionException | IllegalArgumentException e) {
                throw new TslApiException(
                    "特斯联API接口的响应格式不正确: totalPages 不是合法数字, 其值为: " + response.get("totalPages"), e
                );
            }
        }

        /**
         * 解析并追加单页数据
         * <p>
         * 该方法从接口响应中读取 datas 字段，并将其转换为 List&lt;Map&gt; 后追加到 results。
         * 若 datas 缺失或结构不符合预期，将抛出异常以提示响应格式错误。
         *
         * @param results 累计结果（会在此基础上追加）
         * @param response 接口响应
         * @throws TslApiException 当 datas 缺失或无法解析时抛出
         */
        private void addPage(List<Map<String, Object>> results, Map<String, Object> response) throws TslApiException {
            Object obj = response.get("datas");
            if (obj == null) throw new TslApiException("特斯联API接口的响应格式不正确: 缺少 datas 字段");
            try {
                List<Map<String, Object>> pageData = objectMapper.convertValue(obj, new TypeReference<>() {});
                results.addAll(pageData);
            } catch (IllegalArgumentException e) {
                String msg = String.format(
                    "解析特斯联API接口的响应datas字段失败: %s\n响应体:\n%s", e.getMessage(), response
                );
                throw new TslApiException(msg, e);
            }
        }

        /**
         * 请求特斯联事件查询接口
         *
         * @param startTime 起始时间
         * @param pageNum 页码
         * @param pageSize 每页数量
         * @return 接口返回数据
         */
        private Map<String, Object> postEndpoint(
            @NotNull LocalDateTime startTime,
            @Min(1) int pageNum,
            @Min(1) @Max(100) int pageSize
        ) throws TslApiException {
            Map<String, Object> postBody = Map.of(
                "startTime", startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                "pageNum", pageNum,
                "pageSize", pageSize,
                "filterMerge", 2,
                "status", 5
            );
            return tslApiUtil.fetch(
                restClient -> restClient
                    .post()
                    .uri(tslBaseUrl + tslApiUrl)
                    .headers(tslApiUtil.buildHeaders(null))
                    .body(postBody)
                    .retrieve()
                , timeoutSec
            );
        }

    }
}
