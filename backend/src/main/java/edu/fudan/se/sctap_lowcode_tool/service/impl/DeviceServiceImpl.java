package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceRepository;
import edu.fudan.se.sctap_lowcode_tool.service.DeviceService;
import edu.fudan.se.sctap_lowcode_tool.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceServiceImpl implements DeviceService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private JsonUtil jsonUtil;

    @Override
    public DeviceInfo saveOrUpdateDevice(DeviceInfo deviceInfo) {
        return deviceRepository.save(deviceInfo);
    }

    @Override
    public boolean deleteDevice(String deviceID) {
        if (deviceRepository.existsById(deviceID)) {
            deviceRepository.deleteById(deviceID);
            return true;
        }
        return false;
    }

    @Override
    public Optional<DeviceInfo> findById(String deviceID) {
        return deviceRepository.findById(deviceID);
    }

    @Override
    public String getDeviceStatus(String deviceID) {
        return deviceRepository.findById(deviceID)
                .map(DeviceInfo::getStatus)
                .orElse("Device not found");
    }

    @Override
    public String getDeviceURL(String deviceID) {
        return deviceRepository.findById(deviceID)
                .map(DeviceInfo::getUrl)
                .orElse("Device not found");
    }

    @Override
    public String getDeviceData(String deviceID) {
        return deviceRepository.findById(deviceID)
                .map(DeviceInfo::getData)
                .orElse("Device not found");
    }

    @Override
    public String getDeviceType(String deviceID) {
        return deviceRepository.findById(deviceID)
                .map(DeviceInfo::getType)
                .orElse("Device not found");
    }

    @Override
    public Boolean getDeviceIsSensor(String deviceID) {
        return deviceRepository.findById(deviceID)
                .map(DeviceInfo::getIsSensor)
                .orElse(false);
    }

    @Override
    public String getDeviceCapabilities(String deviceID) {
        return deviceRepository.findById(deviceID)
                .map(DeviceInfo::getCapabilities)
                .orElse("Device not found");
    }

    @Override
    public Iterable<DeviceInfo> findAll() {
        return Optional.of(deviceRepository.findAll())
                .orElseGet(Collections::emptyList);
    }

    @Override
    public boolean importDevices(String json) {
        return Optional.ofNullable(json)
                .map(j -> jsonUtil.parseJsonToList(j, DeviceInfo.class))
                .map(devices -> {
                    devices.forEach(this::saveOrUpdateDevice);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public Optional<String> exportDevices() {
        return Optional.ofNullable(findAll())
                .map(devices -> jsonUtil.convertListToJson((List<DeviceInfo>) devices));
    }
}