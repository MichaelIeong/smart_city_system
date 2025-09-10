package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

@Data
public class AppRuleUpdateRequest {
    private Integer id;
    private String description;
    private String flowJson;
}
