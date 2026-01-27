package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeWithFunctionsDTO;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceTypeInfo;
import edu.fudan.se.sctap_lowcode_tool.model.TslProduct;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.ActuatingFunctionNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.DeviceTypeNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.SpaceNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.DeviceTypeNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.SpaceNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceTypeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.TslProductRepository;
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
    private TslProductRepository tslProductRepository;

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
     * 新增设备类型
     * 处理 JSON 字段的空值问题，防止数据库报错
     */
    @Transactional("jpaTransactionManager")
    public Map<String, Object> addDeviceType(Map<String, Object> productData) {
        // 1. 获取参数时需要强转为 (String)
        String productId = (String) productData.get("deviceTypeId");
        String productName = (String) productData.get("deviceTypeName");
        String meshNature = (String) productData.get("mesh_nature");
        if (productId == null || productId.isEmpty() || productName == null || productName.isEmpty()) {
            throw new IllegalArgumentException("设备类型ID和名称不能为空。");
        }

        if (tslProductRepository.existsById(productId)) {
            throw new IllegalArgumentException("设备类型ID " + productId + " 已存在！");
        }

        // 2. 场景映射逻辑 (将 F-city 等映射为数据库的 project_id)
        int projectId = 0;
        if ("F-city".equals(meshNature)) {
            projectId = 1;
        } else if ("F-community".equals(meshNature)) {
            projectId = 2;
        } else if ("F-park".equals(meshNature)) {
            projectId = 3;
        }

        // 3. 格式化 JSON 字段 (注意强转)
        String propertyJson = formatStringToArrayJson((String) productData.get("deviceTypeAttributes"));
        String functionJson = formatStringToArrayJson((String) productData.get("deviceTypeFunction"));
        String instructionJson = formatStringToArrayJson((String) productData.get("deviceTypeInstruction"));
        String eventJson = formatStringToArrayJson((String) productData.get("deviceTypeEvent"));

        String rawProductJson = (String) productData.get("productJson");
        if (rawProductJson != null && rawProductJson.trim().isEmpty()) {
            rawProductJson = null;
        }


        TslProduct product = new TslProduct();
        product.setProductId(productId);
        product.setProductName(productName);
        product.setProductProperty(propertyJson);
        product.setProductFunction(functionJson);
        product.setProductInstruction(instructionJson);
        product.setProductEvent(eventJson);
        product.setProductJson(rawProductJson); // 此时它要么是 valid json，要么是 null
        product.setProjectId(projectId);

        try {
            tslProductRepository.save(product);
        } catch (Exception e) {
            // 捕获数据库层面的 JSON 格式错误，返回更易读的提示
            if (e.getMessage().contains("Invalid JSON text")) {
                throw new IllegalArgumentException("提交失败：Product JSON 内容格式不正确，请检查双引号和冒号。");
            }
            throw e;
        }

        return Map.of("success", true, "message", "设备类型添加成功", "productId", productId);
    }

    /**
     * 获取设备类型
     */
    public List<TslProduct> getTslProductsByScene(String meshNature) {
        // 1. 确定 Project ID (用于查定义)
        int projectId = 0;
        if ("F-city".equals(meshNature)) projectId = 1;
        else if ("F-community".equals(meshNature)) projectId = 2;
        else if ("F-park".equals(meshNature)) projectId = 3;

        if (meshNature == null || meshNature.isEmpty()) {
            return tslProductRepository.findAll();
        }

        // 参数1: projectId (查新定义的)
        // 参数2: meshNature (查正在使用的旧数据)
        return tslProductRepository.findBySceneDefinitionOrUsage(projectId, meshNature);
    }
    /**
     * 删除设备类型 (tsl_product)
     */
    @Transactional("jpaTransactionManager")
    public void deleteDeviceTypeTsl(String productId) {
        if (!tslProductRepository.existsById(productId)) {
            throw new IllegalArgumentException("设备类型不存在: " + productId);
        }
        tslProductRepository.deleteById(productId);
    }

    /**
     * 辅助工具：将逗号/换行分隔的字符串 -> JSON数组字符串
     * 输入: "A, B" -> 输出: "[\"A\",\"B\"]"
     */
    private String formatStringToArrayJson(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null; // 或者返回 "[]"，视数据库约束而定
        }
        // 如果用户已经输入了 JSON 格式 (例如 ["A","B"])，则直接返回，防止二次转义
        if (input.trim().startsWith("[")) {
            return input;
        }

        String[] elements = input.split("[,\\n\\r]+");
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.length; i++) {
            String val = elements[i].trim();
            if (!val.isEmpty()) {
                sb.append("\"").append(val).append("\"");
                if (i < elements.length - 1) {
                    sb.append(",");
                }
            }
        }
        // 处理末尾多余逗号的情况
        if (sb.length() > 1 && sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
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