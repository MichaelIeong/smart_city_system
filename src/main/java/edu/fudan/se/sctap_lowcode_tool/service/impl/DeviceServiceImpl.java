package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import edu.fudan.se.sctap_lowcode_tool.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class DeviceServiceImpl implements DeviceService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Override
    public DeviceInfo saveOrUpdateDevice(DeviceInfo deviceInfo) {
        int spaceId = spaceRepository.findBySpaceName(deviceInfo.getSpaceName()).get().getSpaceId();
        deviceInfo.setSpaceId(spaceId);
        return deviceRepository.save(deviceInfo);
    }

    @Override
    public boolean deleteDevice(int deviceID) {
        if (deviceRepository.existsById(deviceID)) {
            deviceRepository.deleteById(deviceID);
            return true;
        }
        return false;
    }

    @Override
    public Optional<DeviceInfo> findById(int deviceID) {
        return deviceRepository.findById(deviceID);
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
    public Iterable<DeviceInfo> findAll() {
        return Optional.of(deviceRepository.findAll())
                .orElseGet(Collections::emptyList);
    }
}