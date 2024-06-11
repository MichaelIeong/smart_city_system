package edu.fudan.se.sctap_lowcode_tool.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Action {

    @JsonProperty("action_name")
    private String action_name;

    @JsonProperty("action_location")
    private List<String> action_location;

    @JsonProperty("action_param")
    private String action_param;

    public Action(String action_name, List<String> action_location, String action_param) {
        this.action_name = action_name;
        this.action_location = action_location;
        this.action_param = action_param;
    }

    public String getAction_name() {
        return action_name;
    }

    public void setAction_name(String action_name) {
        this.action_name = action_name;
    }

    public List<String> getAction_location() {
        return action_location;
    }

    public void setAction_location(List<String> action_location) {
        this.action_location = action_location;
    }

    public String getAction_param() {
        return action_param;
    }

    public void setAction_param(String action_param) {
        this.action_param = action_param;
    }
}
