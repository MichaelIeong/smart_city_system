package edu.fudan.se.sctap_lowcode_tool.DTO.app;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Event {
    private String event_type;

    private Map<String, String> params;
}
