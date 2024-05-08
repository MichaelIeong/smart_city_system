package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;

public interface DeviceService {
    DeviceInfo updateDeviceInfo(DeviceInfo deviceInfo);
    String getDeviceStatus(int deviceId);
    String getDeviceURL(int deviceId);
    String getDeviceData(int deviceId);
    String getDeviceCapabilities(int deviceId);
}
