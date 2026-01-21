package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.core.type.TypeReference;
import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceCreateRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceResponse;
import edu.fudan.se.sctap_lowcode_tool.model.*;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.*;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.ActuatingFunctionNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.DeviceNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.SpaceNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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

    @Autowired
    private ActuatingFunctionNodeRepository actuatingFunctionNodeRepository;   // Neo4j

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
//        DeviceCreateRequest req = convertToRequest(saved);
//        create(req);
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


    private DeviceCreateRequest convertToRequest(DeviceInfo info) {
        DeviceCreateRequest req = new DeviceCreateRequest();
        req.setDeviceId(info.getId()); // 注意：这里是 MySQL 自增的 PK？还是 deviceId？
        req.setDeviceName(info.getDeviceName());
        req.setDescription(info.getFixedProperties()); // 或者另建字段映射

        if (info.getSpace() != null) {
            req.setSpaceId(info.getSpace().getSpaceId());
        }
        if (info.getDeviceType() != null) {
            req.setDeviceTypeId(info.getDeviceType().getId());
        }

        // 如果 DeviceInfo.states / actuatingFunctions 有对应关系，可以转成 FunctionBinding
        // 这里暂时跳过，后面再细化
        return req;
    }
    /**
     * 创建设备（推荐：只用主键构建 stub，另做存在性校验）
     */
//    @Transactional
//    public DeviceNode create(DeviceCreateRequest req) {
//        DeviceNode d = new DeviceNode();
//        d.setDeviceId(req.getDeviceId());
//        d.setDeviceName(req.getDeviceName());
//        d.setDescription(req.getDescription());
//
//        // --- 方式 A：仅用 id 构建 stub，避免额外查询 ---
//        // 可选：在生产里建议加 existsById 校验（避免“悬挂关系”）
//        if (!spaceNodeRepository.existsById(req.getSpaceId())) {
//            throw new IllegalArgumentException("Space not found: " + req.getSpaceId());
//        }
//        if (!deviceNodeRepository.existsById(req.getDeviceTypeId())) {
//            throw new IllegalArgumentException("DeviceType not found: " + req.getDeviceTypeId());
//        }
//        SpaceNode spaceStub = new SpaceNode();
//        spaceStub.setSpaceId(req.getSpaceId());
//        d.setLocatedIn(spaceStub);
//
//        DeviceTypeNode typeStub = new DeviceTypeNode();
//        typeStub.setDeviceTypeId(req.getDeviceTypeId());
//        d.setDeviceType(typeStub);
//
//        // 设备-功能 带属性关系
//        if (req.getFunctions() != null && !req.getFunctions().isEmpty()) {
//            d.setActuatingFunctions(new HashSet<>());
//            for (DeviceCreateRequest.FunctionBinding fb : req.getFunctions()) {
//                if (!actuatingFunctionNodeRepository.existsById(fb.getActuatingFunctionId())) {
//                    throw new IllegalArgumentException("ActuatingFunction not found: " + fb.getActuatingFunctionId());
//                }
//                ActuatingFunctionNode afStub = new ActuatingFunctionNode();
//                afStub.setActuatingFunctionId(fb.getActuatingFunctionId());
//
//                ActuatingFunctionDeviceRelation rel = new ActuatingFunctionDeviceRelation();
//                rel.setActuatingFunction(afStub);
//                rel.setUrl(fb.getUrl());
//                rel.setDescription(fb.getDescription());
//
//                d.getActuatingFunctions().add(rel);
//            }
//        }
//
//        return deviceNodeRepository.save(d);
//    }

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