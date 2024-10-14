package edu.fudan.se.sctap_lowcode_tool.DTO;

import edu.fudan.se.sctap_lowcode_tool.model.ServiceInfo;

public record ServiceBriefResponse(
        Integer id,
        String serviceId,
        String serviceName
) {
    public ServiceBriefResponse(ServiceInfo serviceInfo) {
        this(serviceInfo.getId(), serviceInfo.getServiceId(), serviceInfo.getServiceName());
    }
}
