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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;



@Service
public class DeviceTypeService {

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private DeviceTypeNodeRepository deviceTypeNodeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    /**
     * 新增设备类型到 tsl_product 表
     * @param productData 包含 deviceTypeId, deviceTypeName, deviceTypeAttributes, deviceTypeFunction
     * @return 成功信息
     */
    public Map<String, Object> addDeviceType(Map<String, String> productData) {
        String productId = productData.get("deviceTypeId");
        String productName = productData.get("deviceTypeName");
        // 映射：使用 product_property 字段
        String productProperty = productData.get("deviceTypeAttributes");
        // 映射：使用 product_function 字段
        String productFunction = productData.get("deviceTypeFunction");

        if (productId == null || productId.isEmpty() || productName == null || productName.isEmpty()) {
            throw new IllegalArgumentException("设备类型序号和名称不能为空。");
        }

        // 格式化：将前端传入的属性和功能字符串转换为 JSON 数组格式
        String propertyJson = formatStringToArrayJson(productProperty);
        String functionJson = formatStringToArrayJson(productFunction);

        // 插入 SQL 使用 product_property 和 product_function
        String sql = "INSERT INTO tsl_product (product_id, product_name, product_property, product_function) " +
                "VALUES (?, ?, ?, ?)";

        int updated = jdbcTemplate.update(sql,
                productId,
                productName,
                propertyJson,
                functionJson);

        if (updated > 0) {
            return Map.of("success", true, "message", "设备类型添加成功", "productId", productId);
        } else {
            throw new RuntimeException("设备类型添加失败，数据库未更新。");
        }
    }

    // 辅助方法：将逗号或换行分隔的字符串转换为简单的 JSON 数组字符串 (保持与之前提供的代码一致)
    private String formatStringToArrayJson(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "[]";
        }
        String[] elements = input.split("[,\\n\\r]+");
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.length; i++) {
            sb.append("\"").append(elements[i].trim()).append("\"");
            if (i < elements.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    // neo4j
    // 功能 1：新增设备类型节点
//    public DeviceTypeNode createDeviceType(DeviceTypeNode dto) {
//        // 如果传进来的 id 已存在，可以选择抛异常或直接返回已有节点
//        if (deviceTypeNodeRepository.existsById(dto.getDeviceTypeId())) {
//            throw new IllegalArgumentException("DeviceType already exists: " + dto.getDeviceTypeId());
//        }
//        return deviceTypeNodeRepository.save(dto);
//    }

    // space和设备类型建立关系
//    @Transactional
//    public void addDeviceTypeToSpace(Integer deviceTypeId, Integer spaceId) {
//        DeviceTypeNode deviceType = deviceTypeNodeRepository.findById(deviceTypeId)
//                .orElseThrow(() -> new IllegalArgumentException("DeviceType not found: " + deviceTypeId));
//
//        SpaceNode space = spaceNodeRepository.findById(spaceId)
//                .orElseThrow(() -> new IllegalArgumentException("Space not found: " + spaceId));
//
//        deviceType.addSpace(space);
//
//        deviceTypeNodeRepository.save(deviceType);
//    }

    // 查询设备类型和类型方法
//    public List<DeviceTypeWithFunctionsDTO> listDeviceTypesAndFunctionsBySpace(Integer spaceId) {
//        List<DeviceTypeNode> types = deviceTypeNodeRepository.findDeviceTypesWithFunctionsBySpaceId(spaceId);
//
//        List<DeviceTypeWithFunctionsDTO> result = new ArrayList<>();
//        for (DeviceTypeNode dt : types) {
//            DeviceTypeWithFunctionsDTO dto = new DeviceTypeWithFunctionsDTO();
//            dto.setDeviceTypeId(dt.getDeviceTypeId());
//            dto.setDeviceTypeName(dt.getDeviceTypeName());
//            dto.setIsSensor(dt.getIsSensor());
//
//            List<DeviceTypeWithFunctionsDTO.ActuatingFunctionDTO> functions =
//                    (dt.getFunctions() == null ? List.<ActuatingFunctionNode>of() : dt.getFunctions())
//                            .stream()
//                            .sorted(Comparator.comparing(ActuatingFunctionNode::getFunctionName, Comparator.nullsLast(String::compareTo)))
//                            .map(af -> {
//                                DeviceTypeWithFunctionsDTO.ActuatingFunctionDTO x = new DeviceTypeWithFunctionsDTO.ActuatingFunctionDTO();
//                                x.setActuatingFunctionId(af.getActuatingFunctionId());
//                                x.setFunctionName(af.getFunctionName());
//                                x.setDescription(af.getDescription());
//                                return x;
//                            })
//                            .collect(Collectors.toList());
//
//            dto.setFunctions(functions);
//            result.add(dto);
//        }
//        return result;
//    }
}