package edu.fudan.se.sctap_lowcode_tool.DTO;

public record AppRuleCreateRequest(
        Integer projectId,
        String description,
        String ruleJson
) {
}
