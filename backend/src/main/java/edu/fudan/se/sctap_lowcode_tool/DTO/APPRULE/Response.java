package edu.fudan.se.sctap_lowcode_tool.DTO.APPRULE;

import lombok.Data;

import java.util.List;

@Data
public class Response {
    private List<ChainStep> chain;
    private List<BranchNode> branch;
    public boolean isChainType() {
        return chain != null && !chain.isEmpty();
    }

    public boolean isBranchType() {
        return branch != null && !branch.isEmpty();
    }
}
