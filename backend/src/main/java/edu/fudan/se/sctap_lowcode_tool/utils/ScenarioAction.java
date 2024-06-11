package edu.fudan.se.sctap_lowcode_tool.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    public String getHistory_condition() {
        return history_condition;
    }

    public void setHistory_condition(String history_condition) {
        this.history_condition = history_condition;
    }

    public String getcurrent_condition() {
        return current_condition;
    }

    public void setcurrent_condition(String current_condition) {
        this.current_condition = current_condition;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "ScenarioAction{" +
                "history_condition='" + history_condition + '\'' +
                ", current_condition='" + current_condition + '\'' +
                ", action=" + action +
                '}';
    }
}

