package edu.fudan.se.sctap_lowcode_tool.DTO.app;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Trigger {
    private List<Event> event;

    private List<Map<String, Object>> filter;
}
