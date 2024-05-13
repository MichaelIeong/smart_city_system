package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;

import java.util.ArrayList;

public interface DeviceService {
    void updateDeviceInfo(DeviceInfo deviceInfo);
    String getDeviceStatus(int deviceID);
    String getDeviceURL(int deviceID);
    JsonNode getDeviceData(int deviceID);
    ArrayList<String> getDeviceCapabilities(int deviceID);
}
