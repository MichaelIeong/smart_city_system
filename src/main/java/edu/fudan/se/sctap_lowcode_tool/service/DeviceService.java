package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;

import java.util.ArrayList;

public interface DeviceService {
    boolean updateDeviceInfo(DeviceInfo deviceInfo);

    String getDeviceStatus(int deviceId);
    String getDeviceURL(int deviceId);
    JsonNode getDeviceData(int deviceId);
    ArrayList<String> getDeviceCapabilities(int deviceId);
}
