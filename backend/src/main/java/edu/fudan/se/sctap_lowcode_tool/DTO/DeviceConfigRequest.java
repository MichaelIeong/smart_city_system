package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

@Data
public class DeviceConfigRequest {
    private int deviceId;
    private String deviceName;
    private DeviceConfig deviceConfig;
}
