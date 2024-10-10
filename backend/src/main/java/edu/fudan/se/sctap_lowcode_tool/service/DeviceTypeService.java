package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeResponse;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceTypeService {

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    public Optional<DeviceTypeResponse> getDeviceTypeById(int id) {
        return deviceTypeRepository.findById(id).map(DeviceTypeResponse::new);
    }

    public List<DeviceTypeResponse> getDevicesByProjectId(int projectId) {
        return deviceTypeRepository.findByProjectInfoProjectId(projectId)
                .stream()
                .map(DeviceTypeResponse::new)
                .toList();
    }

}
