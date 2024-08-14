package edu.fudan.se.sctap_lowcode_tool.utils;

/**
 * @author ：sunlinyue
 * @date ：Created in 2024/5/15 21:35
 * @description：
 * @modified By：
 * @version: $
 */
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ScenarioAction {

    @JsonProperty("history_condition")
    private String history_condition;

    @JsonProperty("current_condition")
    private String current_condition;

    @JsonProperty("action")
    private Action action;

    public ScenarioAction() {}

    public ScenarioAction(String history_condition, String current_condition, Action action) {
        this.history_condition = history_condition;
        this.current_condition = current_condition;
        this.action = action;
    }
}

