package edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * <h3>BaseEvent 事件融合流水线事件基类</h3>
 * 采集器需将采集到的原始事件转换为该类型后进入流水线。
 * <p>
 * 当前包含两类子事件：
 * <ul>
 *   <li>DataEvent：业务数据事件，携带事件来源、标识与载荷数据等。</li>
 *   <li>ControlEvent：控制事件，不携带事件数据，用于驱动或控制流水线行为。</li>
 * </ul>
 * @author Lin Yicheng
 * @since 2026-01-16
 */
@Data
@SuperBuilder
public class BaseEvent {
    /**
     * 事件产生时间戳（Unix毫秒级）
     */
    private final long timestamp;
    /**
     * 事件来源的采集器标识（由 Ingestor 指定）
     */
    @NotNull private final String sourceIngestor;
}
