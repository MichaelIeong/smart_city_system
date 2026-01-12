package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

import java.util.Map;

@Data
public class EventTriggerRequest {
    private String event_type;

    private Map<String, Object> event_params;
}
