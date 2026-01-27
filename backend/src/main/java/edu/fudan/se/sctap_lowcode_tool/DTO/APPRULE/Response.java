package edu.fudan.se.sctap_lowcode_tool.DTO.APPRULE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private List<ChainStep> chain;
    private List<BranchNode> branch;

    @JsonIgnore
    public boolean isChainType() {
        return chain != null && !chain.isEmpty();
    }

    @JsonIgnore
    public boolean isBranchType() {
        return branch != null && !branch.isEmpty();
    }
}
