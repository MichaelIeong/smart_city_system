package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

@Data
public class RuleGenerateRequest {
    private String uuid;
    private String message;
    private String gridId;
}
