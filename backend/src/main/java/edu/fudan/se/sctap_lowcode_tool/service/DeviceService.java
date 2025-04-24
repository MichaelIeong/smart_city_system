package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceResponse;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    public Optional<DeviceResponse> findById(int id) {
        return deviceRepository.findById(id).map(DeviceResponse::new);
    }

    public List<DeviceResponse> findAllByProjectId(int projectId) {
        return deviceRepository.findAllByProjectId(projectId)
                .stream().map(DeviceResponse::new).toList();
    }

    public Optional<DeviceResponse> findByDeviceId(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId).map(DeviceResponse::new);
    }

}