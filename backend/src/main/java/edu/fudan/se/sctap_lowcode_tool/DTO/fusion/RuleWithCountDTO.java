package edu.fudan.se.sctap_lowcode_tool.DTO.fusion;

import lombok.Data;

@Data
public class RuleWithCountDTO {
    private int ruleId;
    private String ruleName;
    private long branchCount;
}