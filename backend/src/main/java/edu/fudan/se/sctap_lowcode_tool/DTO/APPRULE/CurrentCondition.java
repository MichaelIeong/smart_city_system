package edu.fudan.se.sctap_lowcode_tool.DTO.APPRULE;

import lombok.Data;

@Data
public class CurrentCondition {
    private CurrentLeft current_left;
    private String operator;
    private String right;

    @Data
    public static class CurrentLeft {
        private String type;

        private String property;
    }
}
