package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

@Data
public class AppRuleExecuteDetail {
    private Integer id;
    private String gridId;
    private String meshNo;
    private String meshName;
    private Boolean enabled;
}
