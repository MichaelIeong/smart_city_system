package edu.fudan.se.sctap_lowcode_tool.DTO.fusion;

import lombok.Data;

@Data
public class CreateBranchReqDTO {
    private Integer ruleId;
    private Integer spaceId;    // 可为null
    private String branchName;  // 可为null → 默认使用“主干名 + 序号”
    private String fusionTarget;
    private String status;      // 可为null → 默认“inactive”
    private String ruleJson;
    private String flowJson;
    private String remark;
}