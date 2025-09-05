package edu.fudan.se.sctap_lowcode_tool.DTO.APPRULE;

import lombok.Data;

import java.util.Map;

@Data
public class Trigger {
    private String event_type;
    private Map<String, String> event_params;
}
