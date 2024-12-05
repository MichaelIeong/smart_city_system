package edu.fudan.se.sctap_lowcode_tool.DTO;

public record AppRuleRunRequest(
        String eventId,
        String spaceId,
        Integer projectId
) {
}
