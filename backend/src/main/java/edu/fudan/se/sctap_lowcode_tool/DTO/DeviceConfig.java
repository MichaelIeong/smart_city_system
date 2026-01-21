package edu.fudan.se.sctap_lowcode_tool.DTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Data
public class DeviceConfig {

    private int deviceId;
    private String deviceName;
    private List<DeviceState> states;
    private Map<String, Object> parameters; //参数
    private Map<String, Object> variables; //全局变量

    private String productId;          // 用于后台逻辑
    private String productName;        // 用于显示
    private List<String> functions;    // 对应 product_function，可能是逗号分隔或 JSON 数组
    private String productJson;        // 参数描述

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public List<DeviceState> getStates() {
        return states;
    }

    public void setStates(List<DeviceState> states) {
        this.states = states;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public static DeviceConfig fromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, DeviceConfig.class);
    }

    public String toJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }


}
