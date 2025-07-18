package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeResponse;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceTypeInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.DeviceTypeNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.DeviceTypeNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceTypeService {

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private DeviceTypeNodeRepository deviceTypeNodeRepository;

    public Optional<DeviceTypeResponse> getDeviceTypeById(int id) {
        return deviceTypeRepository.findById(id).map(DeviceTypeResponse::new);
    }

    public List<DeviceTypeResponse> getDevicesByProjectId(int projectId) {
        return deviceTypeRepository.findByProjectInfoProjectId(projectId)
                .stream()
                .map(DeviceTypeResponse::new)
                .toList();
    }

    public DeviceTypeInfo saveDeviceType(DeviceTypeInfo deviceType) {
        DeviceTypeInfo saved = deviceTypeRepository.save(deviceType);

        DeviceTypeNode node = new DeviceTypeNode();
        node.setDeviceTypeId(saved.getDeviceTypeId());
        node.setDeviceTypeName(saved.getDeviceTypeName());
        node.setIsSensor(saved.getIsSensor());

        deviceTypeNodeRepository.save(node);
        return saved;
    }

    public Optional<DeviceTypeInfo> updateDeviceType(int id, DeviceTypeInfo updated) {
        return deviceTypeRepository.findById(id).map(existing -> {
            existing.setDeviceTypeId(updated.getDeviceTypeId());
            existing.setDeviceTypeName(updated.getDeviceTypeName());
            existing.setIsSensor(updated.getIsSensor());
            DeviceTypeInfo saved = deviceTypeRepository.save(existing);

            deviceTypeNodeRepository.findByDeviceTypeId(saved.getDeviceTypeId())
                .ifPresentOrElse(node -> {
                    node.setDeviceTypeName(saved.getDeviceTypeName());
                    node.setIsSensor(saved.getIsSensor());
                    deviceTypeNodeRepository.save(node);
                }, () -> {
                    DeviceTypeNode newNode = new DeviceTypeNode();
                    newNode.setDeviceTypeId(saved.getDeviceTypeId());
                    newNode.setDeviceTypeName(saved.getDeviceTypeName());
                    newNode.setIsSensor(saved.getIsSensor());
                    deviceTypeNodeRepository.save(newNode);
                });

            return saved;
        });
    }

    public void deleteDeviceType(int id) {
        deviceTypeRepository.findById(id).ifPresent(deviceType -> {
            deviceTypeNodeRepository.findByDeviceTypeId(deviceType.getDeviceTypeId())
                    .ifPresent(node -> deviceTypeNodeRepository.deleteById(node.getId()));
        });
        deviceTypeRepository.deleteById(id);
    }
}