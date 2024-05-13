package edu.fudan.se.sctap_lowcode_tool.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceRepository;
import edu.fudan.se.sctap_lowcode_tool.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class DeviceServiceImpl implements DeviceService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public void updateDeviceInfo(DeviceInfo deviceInfo) {
        deviceRepository.updateByPrimaryKey(deviceInfo);
    }

    @Override
    public String getDeviceStatus(int deviceId) {
        DeviceInfo deviceInfo = deviceRepository.selectByPrimaryKey(deviceId);
        //DeviceInfo deviceInfo = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
        return deviceInfo.getStatus();
    }

    @Override
    public String getDeviceURL(int deviceId) {
        DeviceInfo deviceInfo = deviceRepository.selectByPrimaryKey(deviceId);
        if (deviceInfo.getUrl() != null) return deviceInfo.getUrl();
        else return "Device not found";
//        DeviceInfo deviceInfo = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
//        return deviceInfo.getUrl();
    }

    @Override
    public JsonNode getDeviceData(int deviceId) {
        DeviceInfo deviceInfo = deviceRepository.selectByPrimaryKey(deviceId);
        return deviceInfo.getData();
//        DeviceInfo deviceInfo = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
//        return "Data for device " + deviceInfo.getDeviceId(); // 示例数据
    }

    @Override
    public ArrayList<String> getDeviceCapabilities(int deviceId) {
        DeviceInfo deviceInfo = deviceRepository.selectByPrimaryKey(deviceId);
        return deviceInfo.getCapabilities();
//        DeviceInfo deviceInfo = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
//        return deviceInfo.getCapabilities();
    }
}