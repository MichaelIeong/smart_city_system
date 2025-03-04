package edu.fudan.se.sctap_lowcode_tool.DTO;

import edu.fudan.se.sctap_lowcode_tool.model.ServiceInfo;

public record ServiceBriefResponse(
        Integer serviceId,
        String serviceName,
        String serviceJson
) {
    public ServiceBriefResponse(ServiceInfo serviceInfo) {
        this(serviceInfo.getServiceId(), serviceInfo.getServiceName(), serviceInfo.getServiceJson());
    }
}
