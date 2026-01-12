package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

@Data
public class AppRuleSaveRequest {
    private Integer projectId;
    private String description;
    private String ruleJson;
    private String flowJson;
    private String gridId;
    private String appName;
}
