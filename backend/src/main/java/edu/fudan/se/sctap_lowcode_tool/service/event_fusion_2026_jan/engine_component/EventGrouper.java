package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.BaseEvent;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.DataEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * <h3>EventGrouper 事件分组器</h3>
 * 负责将一批事件按照策略分组成多个事件组，为后续规则匹配提供输入。
 * @author Lin Yicheng
 * @since 2026-01-16
 */
public abstract class EventGrouper {
    /**
     * 对事件列表进行分组
     *
     * @param events 待分组事件列表
     * @return 事件分组结果
     */
    public abstract List<List<DataEvent>> apply(List<BaseEvent> events);

    /**
     * <h3>OneEventPerGroup 单事件分组器</h3>
     * 每个 DataEvent 独立成组，非 DataEvent 将被忽略。
     */
    @Component
    public static class OneEventPerGroup extends EventGrouper {
        @Override
        public List<List<DataEvent>> apply(List<BaseEvent> events) {
            return events.stream()
                .map(e -> {
                    if(e instanceof DataEvent dataEvent) {
                        return List.of(dataEvent);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        }
    }
}
