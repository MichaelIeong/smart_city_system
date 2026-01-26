package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ServiceGroupSyncRequest {
    private Integer serviceId;
    private List<String> gridIdList;
}
