package edu.fudan.se.sctap_lowcode_tool.DTO.APPRULE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class BranchStep implements ChainStep{
    private List<BranchNode> branch;

    @Override
    @JsonIgnore
    public String getType() {
        return "branch";
    }
}
