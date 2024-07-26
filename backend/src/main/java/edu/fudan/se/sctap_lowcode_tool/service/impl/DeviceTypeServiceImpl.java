package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceTypeInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceTypeRepository;
import edu.fudan.se.sctap_lowcode_tool.service.DeviceTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class DeviceTypeServiceImpl implements DeviceTypeService {
    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Override
    public DeviceTypeInfo saveOrUpdateDeviceType(DeviceTypeInfo deviceTypeInfo) {
        return deviceTypeRepository.save(deviceTypeInfo);
    }

    @Override
    public boolean deleteType(int deviceID) {
        if (deviceTypeRepository.existsById(deviceID)) {
            deviceTypeRepository.deleteById(deviceID);
            return true;
        }
        return false;
    }

    @Override
    public Optional<DeviceTypeInfo> findById(int deviceID) {
        return deviceTypeRepository.findById(deviceID);
    }

    @Override
    public Boolean getTypeIsSensor(int deviceID) {
        return deviceTypeRepository.findById(deviceID)
                .map(DeviceTypeInfo::getIsSensor)
                .orElse(false);
    }

    @Override
    public String getTypeCapabilities(int deviceID) {
        return deviceTypeRepository.findById(deviceID)
                .map(DeviceTypeInfo::getCapabilities)
                .orElse("Type not found");
    }

    @Override
    public Iterable<DeviceTypeInfo> findAll() {
        return Optional.of(deviceTypeRepository.findAll())
                .orElseGet(Collections::emptyList);
    }
}