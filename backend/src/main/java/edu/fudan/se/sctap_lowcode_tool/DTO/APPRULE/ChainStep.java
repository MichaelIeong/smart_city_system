package edu.fudan.se.sctap_lowcode_tool.DTO.APPRULE;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActionStep.class),
        @JsonSubTypes.Type(value = WaitStep.class),
        @JsonSubTypes.Type(value = BranchStep.class)
})
public interface ChainStep {
    String getType();
}
