package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceResponse;
import edu.fudan.se.sctap_lowcode_tool.model.ActuatingFunctionDevice;
import edu.fudan.se.sctap_lowcode_tool.model.ActuatingFunctionInfo;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        // 檢查 deviceId 是否已存在（MySQL 唯一約束字段）
        Optional<DeviceInfo> existingDevice = deviceRepository.findByDeviceId(device.getDeviceId());
        if (existingDevice.isPresent()) {
            throw new IllegalArgumentException("設備 ID 已存在：" + device.getDeviceId());
        }

        // 設定最後更新時間（若為 null）
        if (device.getLastUpdateTime() == null) {
            device.setLastUpdateTime(LocalDateTime.now());
        }

        // 先確認 space 存在，設置進去（MySQL 關聯）
        if (device.getSpace() != null && device.getSpace().getSpaceId() != null) {
            spaceRepository.findById(device.getSpace().getSpaceId()).ifPresent(device::setSpace);
        }

        // === MySQL 儲存 ===
        DeviceInfo saved = deviceRepository.save(device);

        // === Neo4j 同步儲存 ===
        DeviceNode node = new DeviceNode();
        node.setDeviceId(saved.getDeviceId());
        node.setDeviceName(saved.getDeviceName());
        node.setFixedProperties(saved.getFixedProperties());
        node.setCoordinateX(saved.getCoordinateX());
        node.setCoordinateY(saved.getCoordinateY());
        node.setCoordinateZ(saved.getCoordinateZ());
        node.setLastUpdateTime(saved.getLastUpdateTime());

        if (saved.getSpace() != null) {
            spaceNodeRepository.findBySpaceId(saved.getSpace().getSpaceId())
                    .ifPresent(node::setSpace); // Neo4j 關聯 space
        }

        node.setDeviceType(null); // 如有需要可後續補充對應類型
        deviceNodeRepository.save(node); // 儲存進 Neo4j

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

    public Set<String> getActuatingFunctionNamesBySpace(Integer spaceId) {
        // 取得該空間內所有 device
        List<DeviceInfo> devicesInSpace = deviceRepository.findAll().stream()
                .filter(device -> device.getSpace() != null && device.getSpace().getSpaceId().equals(spaceId))
                .collect(Collectors.toList());

        Set<String> functionNames = new HashSet<>();
        for (DeviceInfo device : devicesInSpace) {
            if (device.getActuatingFunctions() != null) {
                for (ActuatingFunctionDevice afd : device.getActuatingFunctions()) {
                    ActuatingFunctionInfo function = afd.getActuatingFunction();
                    if (function != null && function.getName() != null) {
                        functionNames.add(function.getName());
                    }
                }
            }
        }
        return functionNames;
    }
}