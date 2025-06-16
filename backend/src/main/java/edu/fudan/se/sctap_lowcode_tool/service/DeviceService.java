package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceResponse;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.DeviceNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.SpaceNode;
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

    public DeviceInfo saveDevice(DeviceInfo device) {
        if (device.getLastUpdateTime() == null) {
            device.setLastUpdateTime(LocalDateTime.now());
        }

        if (device.getSpace() != null && device.getSpace().getId() != null) {
            spaceRepository.findById(device.getSpace().getId()).ifPresent(device::setSpace);
        } else {
            device.setSpace(null);
        }

        DeviceInfo saved = deviceRepository.save(device);

        // 同步 Neo4j
        DeviceNode node = new DeviceNode();
        node.setDeviceId(saved.getDeviceId());
        node.setDeviceName(saved.getDeviceName());
        node.setFixedProperties(saved.getFixedProperties());
        node.setCoordinateX(saved.getCoordinateX());
        node.setCoordinateY(saved.getCoordinateY());
        node.setCoordinateZ(saved.getCoordinateZ());
        node.setLastUpdateTime(saved.getLastUpdateTime());

        if (saved.getSpace() != null && saved.getSpace().getSpaceId() != null) {
            spaceNodeRepository.findBySpaceId(saved.getSpace().getSpaceId())
                    .ifPresent(node::setSpace);
        }

        deviceNodeRepository.save(node);

        return saved;
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
                spaceRepository.findById(updatedDevice.getSpace().getId()).ifPresent(existing::setSpace);
            } else {
                existing.setSpace(null);
            }

            DeviceInfo saved = deviceRepository.save(existing);

            // 更新 Neo4j
            deviceNodeRepository.findByDeviceId(saved.getDeviceId()).ifPresentOrElse(node -> {
                node.setDeviceName(saved.getDeviceName());
                node.setFixedProperties(saved.getFixedProperties());
                node.setCoordinateX(saved.getCoordinateX());
                node.setCoordinateY(saved.getCoordinateY());
                node.setCoordinateZ(saved.getCoordinateZ());
                node.setLastUpdateTime(saved.getLastUpdateTime());

                if (saved.getSpace() != null && saved.getSpace().getSpaceId() != null) {
                    spaceNodeRepository.findBySpaceId(saved.getSpace().getSpaceId())
                            .ifPresent(node::setSpace);
                } else {
                    node.setSpace(null);
                }

                deviceNodeRepository.save(node);
            }, () -> {
                // 若未同步過，創建新節點
                DeviceNode newNode = new DeviceNode();
                newNode.setDeviceId(saved.getDeviceId());
                newNode.setDeviceName(saved.getDeviceName());
                newNode.setFixedProperties(saved.getFixedProperties());
                newNode.setCoordinateX(saved.getCoordinateX());
                newNode.setCoordinateY(saved.getCoordinateY());
                newNode.setCoordinateZ(saved.getCoordinateZ());
                newNode.setLastUpdateTime(saved.getLastUpdateTime());

                if (saved.getSpace() != null && saved.getSpace().getSpaceId() != null) {
                    spaceNodeRepository.findBySpaceId(saved.getSpace().getSpaceId())
                            .ifPresent(newNode::setSpace);
                }

                deviceNodeRepository.save(newNode);
            });

            return saved;
        });
    }

    public void deleteDevice(int id) {
        deviceRepository.findById(id).ifPresent(device -> {
            deviceNodeRepository.findByDeviceId(device.getDeviceId()).ifPresent(node -> {
                deviceNodeRepository.deleteById(node.getId());
            });
        });
        deviceRepository.deleteById(id);
    }
}