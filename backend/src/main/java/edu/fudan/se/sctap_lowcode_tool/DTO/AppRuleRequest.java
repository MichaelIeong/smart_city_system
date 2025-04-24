package edu.fudan.se.sctap_lowcode_tool.DTO;

public record AppRuleRequest(
        Integer projectId,
        String description,
        String ruleJson
) {
}
