package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

import java.util.Map;

@Data
public class CallServiceRequest {
    private String serviceName;
    private Map<String, Object> serviceParams;
}
