package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceHistory;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;

import java.util.Set;

public interface DeviceService {
    void updateDeviceInfo(DeviceInfo deviceInfo);
    String getDeviceStatus(int deviceID);
    String getDeviceURL(int deviceID);
    String getDeviceData(int deviceID);
    String getDeviceCapabilities(int deviceID);
//    Set<DeviceHistory> getDeviceHistory(int deviceID);
}
