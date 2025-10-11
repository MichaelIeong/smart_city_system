package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

import java.util.Map;

@Data
public class EventTriggerRequest {
    private Integer projectId;

    private String eventType;

    private Map<String, String> params;
}
