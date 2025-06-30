package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceResponse;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.DeviceNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.DeviceNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.SpaceNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private DeviceNodeRepository deviceNodeRepository;

    @Autowired
    private SpaceNodeRepository spaceNodeRepository;

//    public Optional<DeviceResponse> findById(int id) {
//        return deviceRepository.findById(id).map(DeviceResponse::new);
//    }
    public Optional<DeviceNode> findByDeviceId(Integer deviceId) {
        System.out.println("设备信息"+deviceNodeRepository.findDeviceWithAllRelationsByDeviceId(deviceId));
        return deviceNodeRepository.findDeviceWithAllRelationsByDeviceId(deviceId);
    }


    public List<DeviceResponse> findAllByProjectId(int projectId) {
        return deviceRepository.findAllByProjectId(projectId)
                .stream().map(DeviceResponse::new).toList();
    }

    public Optional<DeviceResponse> findByDeviceId(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId).map(DeviceResponse::new);
    }

    public DeviceInfo saveDevice(DeviceInfo device) {
        if (device.getLastUpdateTime() == null) {
            device.setLastUpdateTime(LocalDateTime.now());
        }

        if (device.getSpace() != null && device.getSpace().getId() != null) {
            spaceRepository.findById(device.getSpace().getId()).ifPresent(device::setSpace);
        } else {
            device.setSpace(null);
        }

        return deviceRepository.save(device);
    }

    public Optional<DeviceInfo> updateDevice(Integer id, DeviceInfo updatedDevice) {
        return deviceRepository.findById(id).map(existing -> {
            existing.setDeviceId(updatedDevice.getDeviceId());
            existing.setDeviceName(updatedDevice.getDeviceName());
            existing.setFixedProperties(updatedDevice.getFixedProperties());
            existing.setCoordinateX(updatedDevice.getCoordinateX());
            existing.setCoordinateY(updatedDevice.getCoordinateY());
            existing.setCoordinateZ(updatedDevice.getCoordinateZ());
            existing.setLastUpdateTime(LocalDateTime.now());
            existing.setDeviceType(updatedDevice.getDeviceType());

            if (updatedDevice.getSpace() != null && updatedDevice.getSpace().getId() != null) {
                Optional<SpaceInfo> spaceOpt = spaceRepository.findById(updatedDevice.getSpace().getId());
                spaceOpt.ifPresent(existing::setSpace);
            } else {
                existing.setSpace(null);
            }

            return deviceRepository.save(existing);
        });
    }

    public void deleteDevice(int id) {
        deviceRepository.deleteById(id);
    }
}