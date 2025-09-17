package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

@Data
public class AppRuleCompleteRequest {
    private String eventType;

    private String value;
}
