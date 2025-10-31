package edu.fudan.se.sctap_lowcode_tool.DTO;

public record SocialResourceRequest(
        String resourceId,
        String resourceType,
        String description,
        String url
) {
}
