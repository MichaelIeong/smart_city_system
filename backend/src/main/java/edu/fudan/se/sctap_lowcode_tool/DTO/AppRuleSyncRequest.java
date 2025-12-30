package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

import java.util.List;

@Data
public class AppRuleSyncRequest {
    private Integer appId;
    private List<String> gridIdList;
}
