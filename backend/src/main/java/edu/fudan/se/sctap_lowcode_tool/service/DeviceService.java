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
    private DeviceRepository deviceRepository;         // MySQL

    @Autowired
    private SpaceRepository spaceRepository;           // MySQL

    @Autowired
    private DeviceNodeRepository deviceNodeRepository; // Neo4j

    @Autowired
    private SpaceNodeRepository spaceNodeRepository;   // Neo4j

    // === Neo4j 查询 ===
    public Optional<DeviceNode> findByDeviceId(String deviceId) {
        return deviceNodeRepository.findDeviceWithAllRelationsByDeviceId(deviceId); // neo4j
    }

    // === MySQL 查询 ===
    public Optional<DeviceResponse> findByDeviceIdFromMySQL(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId)
                .map(DeviceResponse::new); // mysql
    }

    public List<DeviceResponse> findAllByProjectId(int projectId) {
        return deviceRepository.findAllByProjectId(projectId)
                .stream()
                .map(DeviceResponse::new)
                .toList(); // mysql
    }

    // === 创建设备：MySQL + Neo4j ===
    public DeviceInfo saveDevice(DeviceInfo device) {
        if (device.getLastUpdateTime() == null) {
            device.setLastUpdateTime(LocalDateTime.now());
        }

        // MySQL 保存
        if (device.getSpace() != null && device.getSpace().getSpaceId() != null) {
            spaceRepository.findById(device.getSpace().getSpaceId()).ifPresent(device::setSpace);
        }
        DeviceInfo saved = deviceRepository.save(device); // mysql

        // Neo4j 同步保存
        DeviceNode node = new DeviceNode();
        node.setDeviceId(saved.getDeviceId()); // string 类型
        node.setDeviceName(saved.getDeviceName());
        node.setFixedProperties(saved.getFixedProperties());
        node.setCoordinateX(saved.getCoordinateX());
        node.setCoordinateY(saved.getCoordinateY());
        node.setCoordinateZ(saved.getCoordinateZ());
        node.setLastUpdateTime(saved.getLastUpdateTime());

        if (saved.getSpace() != null) {
            spaceNodeRepository.findBySpaceId(saved.getSpace().getSpaceId())
                    .ifPresent(node::setSpace); // neo4j
        }

        node.setDeviceType(null); // 可按需处理
        deviceNodeRepository.save(node); // neo4j
        return saved;
    }

    // === 更新设备：MySQL + Neo4j ===
    public Optional<DeviceInfo> updateDevice(Integer id, DeviceInfo updatedDevice) {
        return deviceRepository.findById(id).map(existing -> {
            // MySQL 更新
            existing.setDeviceName(updatedDevice.getDeviceName());
            existing.setDeviceId(updatedDevice.getDeviceId());
            existing.setFixedProperties(updatedDevice.getFixedProperties());
            existing.setCoordinateX(updatedDevice.getCoordinateX());
            existing.setCoordinateY(updatedDevice.getCoordinateY());
            existing.setCoordinateZ(updatedDevice.getCoordinateZ());
            existing.setLastUpdateTime(LocalDateTime.now());
            existing.setDeviceType(updatedDevice.getDeviceType());

            if (updatedDevice.getSpace() != null) {
                spaceRepository.findById(updatedDevice.getSpace().getSpaceId())
                        .ifPresent(existing::setSpace);
            } else {
                existing.setSpace(null);
            }

            DeviceInfo saved = deviceRepository.save(existing); // mysql

            // Neo4j 更新
            deviceNodeRepository.findDeviceWithAllRelationsByDeviceId(saved.getDeviceId())
                    .ifPresentOrElse(node -> {
                        node.setDeviceName(saved.getDeviceName());
                        node.setFixedProperties(saved.getFixedProperties());
                        node.setCoordinateX(saved.getCoordinateX());
                        node.setCoordinateY(saved.getCoordinateY());
                        node.setCoordinateZ(saved.getCoordinateZ());
                        node.setLastUpdateTime(saved.getLastUpdateTime());

                        if (saved.getSpace() != null) {
                            spaceNodeRepository.findBySpaceId(saved.getSpace().getSpaceId())
                                    .ifPresent(node::setSpace); // neo4j
                        } else {
                            node.setSpace(null);
                        }

                        deviceNodeRepository.save(node); // neo4j
                    }, () -> {
                        DeviceNode newNode = new DeviceNode();
                        newNode.setDeviceId(saved.getDeviceId());
                        newNode.setDeviceName(saved.getDeviceName());
                        newNode.setFixedProperties(saved.getFixedProperties());
                        newNode.setCoordinateX(saved.getCoordinateX());
                        newNode.setCoordinateY(saved.getCoordinateY());
                        newNode.setCoordinateZ(saved.getCoordinateZ());
                        newNode.setLastUpdateTime(saved.getLastUpdateTime());

                        if (saved.getSpace() != null) {
                            spaceNodeRepository.findBySpaceId(saved.getSpace().getSpaceId())
                                    .ifPresent(newNode::setSpace); // neo4j
                        }

                        deviceNodeRepository.save(newNode); // neo4j
                    });

            return saved;
        });
    }

    // === 删除设备：MySQL + Neo4j ===
    public void deleteDevice(Integer id) {
        deviceRepository.findById(id).ifPresent(device -> {
            deviceNodeRepository.deleteByDeviceId(device.getDeviceId()); // neo4j
            deviceRepository.deleteById(id);                             // mysql
        });
    }
}