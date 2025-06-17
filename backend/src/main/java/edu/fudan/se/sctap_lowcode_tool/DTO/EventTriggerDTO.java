package edu.fudan.se.sctap_lowcode_tool.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class EventTriggerDTO {

    private String event_type;

    private Map<String, Object> params;
}
