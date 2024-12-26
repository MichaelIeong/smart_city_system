package edu.fudan.se.sctap_lowcode_tool.DTO;

import edu.fudan.se.sctap_lowcode_tool.model.*;

import java.util.List;

public record DeviceTypeResponse(
        Integer id,
        Integer projectId,
        String deviceTypeId,
        String deviceTypeName,
        Boolean isSensor,
        List<StateInfo> states,
        List<ActuatingFunctionInfo> actuatingFunctions
) {
    public DeviceTypeResponse(DeviceTypeInfo deviceType) {
        this(
                deviceType.getId(),
                deviceType.getProjectInfo().getProjectId(),
                deviceType.getDeviceTypeId(),
                deviceType.getDeviceTypeName(),
                deviceType.getIsSensor(),
                deviceType.getStates()
                        .stream()
                        .map(StateDeviceType::getState)
                        .toList(),
                deviceType.getActuatingFunctions()
                        .stream()
                        .map(ActuatingFunctionDeviceType::getActuatingFunction)
                        .toList()
        );
    }
}
