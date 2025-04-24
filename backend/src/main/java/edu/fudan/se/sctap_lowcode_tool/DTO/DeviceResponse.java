package edu.fudan.se.sctap_lowcode_tool.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public final class DeviceResponse {

    public final Integer id;
    public final String deviceId;
    public final String deviceName;
    public final String deviceTypeId;
    public final String deviceTypeName;
    public final String fixedProperties;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public final Coordinate coordinate;

    public final LocalDateTime lastUpdateTime;
    public final List<DeviceState> states;
    public final List<DeviceFunction> functions;
    @Getter
    public final String spaceId; // 新增字段

    public DeviceResponse(DeviceInfo deviceInfo) {
        this.id = deviceInfo.getId();
        this.deviceId = deviceInfo.getDeviceId();
        this.deviceName = deviceInfo.getDeviceName();
        this.deviceTypeId = deviceInfo.getDeviceType().getDeviceTypeId();
        this.deviceTypeName = deviceInfo.getDeviceType().getDeviceTypeName();
        this.fixedProperties = deviceInfo.getFixedProperties();
        this.lastUpdateTime = deviceInfo.getLastUpdateTime();

        if (deviceInfo.getCoordinateX() != null ||
            deviceInfo.getCoordinateY() != null ||
            deviceInfo.getCoordinateZ() != null) {
            this.coordinate = new Coordinate(
                deviceInfo.getCoordinateX(),
                deviceInfo.getCoordinateY(),
                deviceInfo.getCoordinateZ()
            );
        } else {
            this.coordinate = null;
        }

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

        this.spaceId = deviceInfo.getSpace().getSpaceId(); // 设置 spaceId
    }

    public record Coordinate(Float x, Float y, Float z) {
    }

    public record DeviceState(Integer stateId, String stateKey, String stateValue) {
    }

    public record DeviceFunction(Integer functionId, String functionName, String functionParams, String functionUrl) {
    }
}