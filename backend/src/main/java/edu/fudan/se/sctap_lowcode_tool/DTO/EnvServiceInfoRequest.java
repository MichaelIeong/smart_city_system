package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

@Data
public class EnvServiceInfoRequest {
    private String envServiceName;
    private Integer projectId;
}