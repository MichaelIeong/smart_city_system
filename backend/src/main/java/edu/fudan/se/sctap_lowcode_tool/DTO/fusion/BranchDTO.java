package edu.fudan.se.sctap_lowcode_tool.DTO.fusion;

import edu.fudan.se.sctap_lowcode_tool.model.FusionRuleBranch;
import lombok.Data;

@Data
public class BranchDTO {
    private Integer branchId;
    private Integer branchIndex;
    private String branchName;
    private String fusionTarget;
    private String status;
    private String ruleJson;
    private String flowJson;
    private String remark;
    private Integer spaceId; // 仅返回ID，避免懒加载问题

    public static BranchDTO from(FusionRuleBranch b) {
        BranchDTO dto = new BranchDTO();
        dto.setBranchId(b.getBranchId());
        dto.setBranchIndex(b.getBranchIndex());
        dto.setBranchName(b.getBranchName());
        dto.setFusionTarget(b.getFusionTarget());
        dto.setStatus(b.getStatus());
        dto.setRuleJson(b.getRuleJson());
        dto.setFlowJson(b.getFlowJson());
        dto.setSpaceId(b.getSpaceId());
        return dto;
    }
}