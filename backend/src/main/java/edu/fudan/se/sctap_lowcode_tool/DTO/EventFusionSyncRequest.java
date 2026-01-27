package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

import java.util.List;

@Data
public class EventFusionSyncRequest {
    private Integer eventId;
    private List<String> gridIdList;
}
