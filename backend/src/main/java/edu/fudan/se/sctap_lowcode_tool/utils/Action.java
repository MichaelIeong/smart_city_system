package edu.fudan.se.sctap_lowcode_tool.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
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
}
