//package edu.fudan.se.sctap_lowcode_tool.service.impl;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
//import edu.fudan.se.sctap_lowcode_tool.repository.DeviceRepository;
//import edu.fudan.se.sctap_lowcode_tool.service.DeviceService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//
//@Service
//public class DeviceServiceImpl implements DeviceService {
//
//    @Autowired
//    private DeviceRepository deviceRepository;
//
//    @Override
//    public DeviceInfo updateDeviceInfo(DeviceInfo deviceInfo) {
//        // 假设这里包含了一些业务逻辑
//        return deviceRepository.save(deviceInfo);
//    }
//
//    @Override
//    public String getDeviceStatus(int deviceId) {
//        DeviceInfo deviceInfo = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
//        return deviceInfo.getStatus();
//    }
//
//    @Override
//    public String getDeviceURL(int deviceId) {
//        DeviceInfo deviceInfo = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
//        return deviceInfo.getUrl();
//    }
//
//    @Override
//    public JsonNode getDeviceData(int deviceId) {
//        DeviceInfo deviceInfo = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
//        return  deviceInfo.getData(); // 示例数据
//    }
//
//    @Override
//    public ArrayList<String> getDeviceCapabilities(int deviceId) {
//        DeviceInfo deviceInfo = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
//        return deviceInfo.getCapabilities();
//    }
//}