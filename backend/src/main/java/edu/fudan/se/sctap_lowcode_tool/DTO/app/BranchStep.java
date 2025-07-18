package edu.fudan.se.sctap_lowcode_tool.DTO.app;

import lombok.Data;

import java.util.List;

@Data
public class BranchStep implements ChainStep {

    private List<BranchNode> branch;

}
