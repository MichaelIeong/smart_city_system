package edu.fudan.se.sctap_lowcode_tool.DTO.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BranchNode {
    @JsonProperty("current_condition")
    private List<Condition> currentCondition;

    @JsonProperty("history_condition")
    private List<Condition> historyCondition;

    private List<ChainStep> chain;

    public List<Condition> getEffectiveCondition() {
        return currentCondition != null ? currentCondition : historyCondition;
    }

    public boolean isCurrentCondition() {
        return currentCondition != null;
    }

    public boolean isHistoryCondition() {
        return historyCondition != null;
    }
}
