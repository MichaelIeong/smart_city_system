package edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * <h3>DataEvent 事件融合流水线数据事件</h3>
 * 表示来自传感器或环境的业务数据事件，承载事件来源、标识与载荷数据等。
 * @author Lin Yicheng
 * @since 2026-01-16
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DataEvent extends BaseEvent {
    /**
     * 事件来源类型（传感器事件或环境事件）
     */
    @NotNull private final EventFusionRule.EventSource eventSource;
    /**
     * 事件的唯一标识符<br/>
     * <b>需保证该标识符全局唯一</b>
     */
    @NotNull private final String identifier;
    /**
     * 事件ID，如 "truck_spill" 表示渣土车抛洒事件
     */
    @NotNull private final String eventId;
    /**
     * 事件载荷，以键值对形式存储事件的具体数据内容
     */
    @NotNull private final Map<String, Object> payload;

    @JsonCreator
    public DataEvent(
        @JsonProperty("timestamp") long timestamp,
        @JsonProperty("sourceIngestor") String sourceIngestor,
        @JsonProperty("eventSource") EventFusionRule.EventSource eventSource,
        @JsonProperty("identifier") String identifier,
        @JsonProperty("eventId") String eventId,
        @JsonProperty("payload") Map<String, Object> payload
    ) {
        super(timestamp, sourceIngestor);
        this.eventSource = eventSource;
        this.identifier = identifier;
        this.eventId = eventId;
        this.payload = payload;
    }
}
