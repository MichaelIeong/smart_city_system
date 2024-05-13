package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceHistory;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceHistoryRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceRepository;
import edu.fudan.se.sctap_lowcode_tool.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class DeviceServiceImpl implements DeviceService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceHistoryRepository deviceHistoryRepository;

    @Override
    public void updateDeviceInfo(DeviceInfo deviceInfo) {
        deviceRepository.save(deviceInfo);
    }

    @Override
    public String getDeviceStatus(int deviceID) {
        return deviceRepository.findById(deviceID)
                .map(DeviceInfo::getStatus)
                .orElse("Device not found");
    }

    @Override
    public String getDeviceURL(int deviceID) {
        return deviceRepository.findById(deviceID)
                .map(DeviceInfo::getUrl)
                .orElse("Device not found");
    }

    @Override
    public String getDeviceData(int deviceID) {
        return deviceRepository.findById(deviceID)
                .map(DeviceInfo::getData)
                .orElse("Device not found");
    }

    @Override
    public String getDeviceCapabilities(int deviceID) {
        return deviceRepository.findById(deviceID)
                .map(DeviceInfo::getCapabilities)
                .orElse("Device not found");
    }

    @Override
    public Set<DeviceHistory> getDeviceHistory(int deviceID) {
        try {
            return new HashSet<>(deviceHistoryRepository.findAllByDeviceId(deviceID));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}