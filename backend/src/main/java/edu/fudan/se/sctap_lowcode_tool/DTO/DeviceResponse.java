package edu.fudan.se.sctap_lowcode_tool.DTO;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;

import java.time.LocalDateTime;
import java.util.List;


public final class DeviceResponse {

    public final Integer id;
    public final String deviceName;
    public final String deviceTypeName;
    public final String fixedProperties;
    public final Coordinate coordinate;
    public final LocalDateTime lastUpdateTime;
    public final List<DeviceState> states;
    public final List<DeviceFunction> functions;
    public DeviceResponse(DeviceInfo deviceInfo) {
        this.id = deviceInfo.getId();
        this.deviceName = deviceInfo.getDeviceName();
        this.deviceTypeName = deviceInfo.getDeviceType().getDeviceTypeName();
        this.fixedProperties = deviceInfo.getFixedProperties();
        this.lastUpdateTime = deviceInfo.getLastUpdateTime();
        this.coordinate = new Coordinate(
                deviceInfo.getCoordinateX(),
                deviceInfo.getCoordinateY(),
                deviceInfo.getCoordinateZ()
        );
        this.states = deviceInfo.getStates().stream().map(state -> new DeviceState(
                state.getState().getStateId(),
                state.getState().getStateKey(),
                state.getStateValue()
        )).toList();
        this.functions = deviceInfo.getActuatingFunctions().stream().map(actuatingFunction -> new DeviceFunction(
                actuatingFunction.getActuatingFunction().getId(),
                actuatingFunction.getActuatingFunction().getName(),
                actuatingFunction.getActuatingFunction().getParams(),
                actuatingFunction.getUrl()
        )).toList();
    }

    public record Coordinate(float x, float y, float z) {
    }

    public record DeviceState(Integer stateId, String stateKey, String stateValue) {
    }

    public record DeviceFunction(Integer functionId, String functionName, String functionParams, String functionUrl) {
    }
}
