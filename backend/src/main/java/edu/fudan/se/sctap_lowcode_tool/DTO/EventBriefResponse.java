package edu.fudan.se.sctap_lowcode_tool.DTO;

import edu.fudan.se.sctap_lowcode_tool.model.EventInfo;

public record EventBriefResponse(
        Integer id,
        String eventId,
        String eventType
) {
    public EventBriefResponse(EventInfo eventInfo) {
        this(eventInfo.getId(), eventInfo.getEventId(), eventInfo.getEventType());
    }
}
