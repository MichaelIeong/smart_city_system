package edu.fudan.se.sctap_lowcode_tool.DTO;

import java.util.Map;

public record AppRuleRunRequest(
    String eventId,
    String spaceId,
    Integer projectId,
    Map<String, String> eventData
) {
}
