package edu.fudan.se.sctap_lowcode_tool.DTO.APPRULE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BranchNode {
    private CurrentCondition current_condition;

    private HistoryCondition history_condition;

    private List<ChainStep> chain;

    @JsonIgnore
    public boolean isCurrentCondition() {
        return current_condition != null;
    }

    @JsonIgnore
    public boolean isHistoryCondition() {
        return history_condition != null;
    }
}
