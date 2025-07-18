package edu.fudan.se.sctap_lowcode_tool.DTO.app;

import lombok.Data;

@Data
public class AppRule {
    private Trigger trigger;

    private Response response;
}
