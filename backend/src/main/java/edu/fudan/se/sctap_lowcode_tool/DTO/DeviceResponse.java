package edu.fudan.se.sctap_lowcode_tool.DTO;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;

import java.util.List;


public final class DeviceResponse {

    public final Integer id;
    public final String deviceId;
    public final String deviceName;
    public final String deviceTypeId;
    public final String deviceTypeName;
    public final String fixedProperties;
    public final Coordinate coordinate;
    public final List<DeviceState> states;
    public final List<DeviceFunction> functions;
    public DeviceResponse(DeviceInfo deviceInfo) {
        this.id = deviceInfo.getId();
        this.deviceId = deviceInfo.getDeviceId();
        this.deviceName = deviceInfo.getDeviceName();
        this.deviceTypeId = deviceInfo.getDeviceType().getDeviceTypeId();
        this.deviceTypeName = deviceInfo.getDeviceType().getDeviceTypeName();
        this.fixedProperties = deviceInfo.getFixedProperties();
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

    @Override
    public String toString() {
        return "DeviceResponse{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceTypeId='" + deviceTypeId + '\'' +
                ", deviceTypeName='" + deviceTypeName + '\'' +
                ", fixedProperties='" + fixedProperties + '\'' +
                ", coordinate=" + coordinate +
                ", states=" + states +
                ", functions=" + functions +
                '}';
    }

    public record Coordinate(float x, float y, float z) {
    }

    public record DeviceState(Integer stateId, String stateKey, String stateValue) {
    }

    public record DeviceFunction(Integer functionId, String functionName, String functionParams, String functionUrl) {
    }
}
