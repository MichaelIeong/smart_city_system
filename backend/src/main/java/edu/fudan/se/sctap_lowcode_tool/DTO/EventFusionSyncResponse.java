package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventFusionSyncResponse {
    private String gridId;
    private String meshNo;
    private String meshName;
    private Integer isSuccess;
    private String message;
}
