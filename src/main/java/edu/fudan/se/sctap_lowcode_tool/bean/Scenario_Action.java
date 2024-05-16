package edu.fudan.se.sctap_lowcode_tool.bean;

/**
 * @author ：sunlinyue
 * @date ：Created in 2024/5/15 21:35
 * @description：
 * @modified By：
 * @version: $
 */
import java.util.List;

public class Scenario_Action {
    private String history_condition;
    private String current_condition;
    private String action_name;
    private List<String> action_location;
    private String action_param;

    public Scenario_Action() {}

    public Scenario_Action(String history_condition, String current_condition, String action_name, List<String> action_location, String action_param) {
        this.history_condition = history_condition;
        this.current_condition = current_condition;
        this.action_name = action_name;
        this.action_location = action_location;
        this.action_param = action_param;
    }

    public String getHistoryCondition() {
        return history_condition;
    }

    public void setHistoryCondition(String history_condition) {
        this.history_condition = history_condition;
    }

    public String getcurrent_condition() {
        return current_condition;
    }

    public void setcurrent_condition(String current_condition) {
        this.current_condition = current_condition;
    }

    public String getaction_name() {
        return action_name;
    }

    public void setaction_name(String action_name) {
        this.action_name = action_name;
    }

    public List<String> getaction_location() {
        return action_location;
    }

    public void setaction_location(List<String> action_location) {
        this.action_location = action_location;
    }

    public String getaction_param() {
        return action_param;
    }

    public void setaction_param(String action_param) {
        this.action_param = action_param;
    }

    @Override
    public String toString() {
        return "ScenarioAction{" +
                "historyCondition='" + history_condition + '\'' +
                ", current_condition='" + current_condition + '\'' +
                ", action_name='" + action_name + '\'' +
                ", action_location=" + action_location +
                ", action_param='" + action_param + '\'' +
                '}';
    }
}

