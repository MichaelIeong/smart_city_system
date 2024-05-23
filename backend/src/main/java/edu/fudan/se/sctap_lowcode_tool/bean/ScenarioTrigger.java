package edu.fudan.se.sctap_lowcode_tool.bean;

/**
 * @author ：sunlinyue
 * @date ：Created in 2024/5/15 21:35
 * @description：
 * @modified By：
 * @version: $
 */
import lombok.Data;

import java.util.List;

@Data
public class ScenarioTrigger {
    private List<List<String>> event_type;
    private List<String> filter;
}

