package edu.fudan.se.sctap_lowcode_tool.DTO.app;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ActionStep implements ChainStep {
    private Action action;

    @Override
    public String getType() {
        return "action";
    }

    @Data
    public static class Action {
        private String action_name;

        private List<String> action_location;

        private Map<String, Object> action_param;
    }
}
