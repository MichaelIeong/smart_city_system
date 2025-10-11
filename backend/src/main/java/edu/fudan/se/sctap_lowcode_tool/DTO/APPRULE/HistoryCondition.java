package edu.fudan.se.sctap_lowcode_tool.DTO.APPRULE;

import lombok.Data;

import java.util.Map;

@Data
public class HistoryCondition {
    private HistoryLeft history_left;
    private String operator;
    private String right;

    @Data
    public static class HistoryLeft {
        private String func;
        private Map<String, String> func_params;
    }
}
