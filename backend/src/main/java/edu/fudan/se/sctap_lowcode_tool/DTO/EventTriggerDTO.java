package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

import java.util.Map;

@Data
public class EventTriggerDTO {

    private String event_type;

    private Map<String, String> params;
}
