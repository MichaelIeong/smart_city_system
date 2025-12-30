package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

@Data
public class AppRuleQueryRequest {
    private String eventType;
    private String description;
    private int pageNo = 1;
    private int pageSize = 20;
    private String sortField;
    private String sortOrder;
}
