package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

@Data
public class AppRuleCompleteRequest {
    private Integer appId;

    private String waitValue;
}
