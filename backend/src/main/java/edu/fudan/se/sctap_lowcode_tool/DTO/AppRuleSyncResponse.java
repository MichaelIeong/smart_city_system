package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppRuleSyncResponse {
    private String gridId;
    private String meshName;
    private Boolean isSuccess;
    private String message;
}
