package edu.fudan.se.sctap_lowcode_tool.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AppRuleData {
    private String rule;
    private Components components;
    @Data
    public static class Components {
        @JsonProperty("event_type")
        private List<String> eventType;

        @JsonProperty("property_type")
        private List<String> propertyType;

        @JsonProperty("action_type")
        private List<String> actionType;
    }
}
