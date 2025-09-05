package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeWithFunctionsDTO;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceTypeInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.ActuatingFunctionNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.DeviceTypeNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.SpaceNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.DeviceTypeNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.SpaceNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DeviceTypeService {

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private DeviceTypeNodeRepository deviceTypeNodeRepository;

    @Autowired
    private SpaceNodeRepository spaceNodeRepository;

    public Optional<DeviceTypeResponse> getDeviceTypeById(int id) {
        return deviceTypeRepository.findById(id).map(DeviceTypeResponse::new);
    }

    public List<DeviceTypeResponse> getDevicesByProjectId(int projectId) {
        return deviceTypeRepository.findByProjectInfoProjectId(projectId)
                .stream()
                .map(DeviceTypeResponse::new)
                .toList();
    }

    public List<DeviceTypeInfo> getDeviceTypeByProjectId(int projectId) {
        return deviceTypeRepository.findByProjectInfoProjectId(projectId);
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

            deviceTypeNodeRepository.findByDeviceTypeId(saved.getDeviceTypeId()))
                .ifPresentOrElse(node -> {
                    node.setDeviceTypeName(saved.getDeviceTypeName());
                    node.setIsSensor(saved.getIsSensor());
                    deviceTypeNodeRepository.save(node);
                }, () -> {
                    DeviceTypeNode newNode = new DeviceTypeNode();
                    newNode.setDeviceTypeId(Integer.valueOf(saved.getDeviceTypeId()));
                    newNode.setDeviceTypeName(saved.getDeviceTypeName());
                    newNode.setIsSensor(saved.getIsSensor());
                    deviceTypeNodeRepository.save(newNode);
                });

            return saved;
        });
    }

    public void deleteDeviceType(int id) {
        deviceTypeRepository.findById(id).ifPresent(deviceType -> {
            deviceTypeNodeRepository.findByDeviceTypeId(Integer.valueOf(deviceType.getDeviceTypeId()))
                    .ifPresent(node -> deviceTypeNodeRepository.deleteById(node.getDeviceTypeId()));
        });
        deviceTypeRepository.deleteById(id);
    }

    // neo4j
    // 功能 1：新增设备类型节点
    public DeviceTypeNode createDeviceType(DeviceTypeNode dto) {
        // 如果传进来的 id 已存在，可以选择抛异常或直接返回已有节点
        if (deviceTypeNodeRepository.existsById(dto.getDeviceTypeId())) {
            throw new IllegalArgumentException("DeviceType already exists: " + dto.getDeviceTypeId());
        }
        return deviceTypeNodeRepository.save(dto);
    }

    // space和设备类型建立关系
    @Transactional
    public void addDeviceTypeToSpace(Integer deviceTypeId, Integer spaceId) {
        DeviceTypeNode deviceType = deviceTypeNodeRepository.findById(deviceTypeId)
                .orElseThrow(() -> new IllegalArgumentException("DeviceType not found: " + deviceTypeId));

        SpaceNode space = spaceNodeRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("Space not found: " + spaceId));

        deviceType.addSpace(space);

        deviceTypeNodeRepository.save(deviceType);
    }

    // 查询设备类型和类型方法
    public List<DeviceTypeWithFunctionsDTO> listDeviceTypesAndFunctionsBySpace(Integer spaceId) {
        List<DeviceTypeNode> types = deviceTypeNodeRepository.findDeviceTypesWithFunctionsBySpaceId(spaceId);

        List<DeviceTypeWithFunctionsDTO> result = new ArrayList<>();
        for (DeviceTypeNode dt : types) {
            DeviceTypeWithFunctionsDTO dto = new DeviceTypeWithFunctionsDTO();
            dto.setDeviceTypeId(dt.getDeviceTypeId());
            dto.setDeviceTypeName(dt.getDeviceTypeName());
            dto.setIsSensor(dt.getIsSensor());

            List<DeviceTypeWithFunctionsDTO.ActuatingFunctionDTO> functions =
                    (dt.getFunctions() == null ? List.<ActuatingFunctionNode>of() : dt.getFunctions())
                            .stream()
                            .sorted(Comparator.comparing(ActuatingFunctionNode::getFunctionName, Comparator.nullsLast(String::compareTo)))
                            .map(af -> {
                                DeviceTypeWithFunctionsDTO.ActuatingFunctionDTO x = new DeviceTypeWithFunctionsDTO.ActuatingFunctionDTO();
                                x.setActuatingFunctionId(af.getActuatingFunctionId());
                                x.setFunctionName(af.getFunctionName());
                                x.setDescription(af.getDescription());
                                return x;
                            })
                            .collect(Collectors.toList());

            dto.setFunctions(functions);
            result.add(dto);
        }
        return result;
    }
}