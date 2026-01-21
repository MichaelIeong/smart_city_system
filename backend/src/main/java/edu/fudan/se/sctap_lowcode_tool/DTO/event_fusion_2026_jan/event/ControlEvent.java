package edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * <h3>ControlEvent 事件融合流水线控制事件</h3>
 * 用于驱动或控制事件融合流水线的内部事件类型。
 * @author Lin Yicheng
 * @since 2026-01-16
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ControlEvent extends BaseEvent {

    /**
     * <h3>EmptyFetchEvent 空拉取事件</h3>
     * 表示采集器向指定数据源拉取数据后，未获取到任何业务事件。
     */
    @Data
    @SuperBuilder
    @EqualsAndHashCode(callSuper = true)
    public static class EmptyFetchEvent extends ControlEvent {}
}
