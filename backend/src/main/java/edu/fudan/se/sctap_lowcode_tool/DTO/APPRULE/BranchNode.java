package edu.fudan.se.sctap_lowcode_tool.DTO.APPRULE;

import lombok.Data;

import java.util.List;

@Data
public class BranchNode {
    private CurrentCondition current_condition;

    private HistoryCondition history_condition;

    private List<ChainStep> chain;

    public boolean isCurrentCondition() {
        return current_condition != null;
    }

    public boolean isHistoryCondition() {
        return history_condition != null;
    }
}
