package edu.fudan.se.sctap_lowcode_tool.DTO.APPRULE;

import lombok.Data;

import java.util.Map;

@Data
public class ActionStep implements ChainStep{
    private Action action;

    @Data
    public static class Action {
        private String action_name;

        private Map<String, String> action_params;
    }
}
