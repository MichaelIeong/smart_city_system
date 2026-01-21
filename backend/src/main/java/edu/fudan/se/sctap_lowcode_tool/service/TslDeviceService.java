package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeSummaryDTO;
import edu.fudan.se.sctap_lowcode_tool.model.TslDevice;
import edu.fudan.se.sctap_lowcode_tool.model.TslProduct;
import edu.fudan.se.sctap_lowcode_tool.repository.TslDeviceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.TslProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TslDeviceService {

    @Autowired
    private TslDeviceRepository tslDeviceRepository;

    @Autowired
    private TslProductRepository tslProductRepository;

    /**
     * 获取场景下的全局设备统计
     */
    public List<DeviceTypeSummaryDTO> getGlobalDeviceSummary(String sceneType) {
        return tslDeviceRepository.findGlobalSummaryByScene(sceneType);
    }

    /**
     * 获取指定网格内的设备统计
     */
    public List<DeviceTypeSummaryDTO> getGridDeviceSummary(String gridId) {
        return tslDeviceRepository.findGridSummaryByGridId(gridId);
    }

    // =========================================================
    // 1. 获取项目下的设备列表 (支持场景过滤)
    //    对应前端: fetchDeviceData -> /api/devices
    // =========================================================
    public List<TslDevice> getDevicesByProject(Long projectId, String meshNature) {
        // 如果前端传了场景ID，则进行过滤
        if (meshNature != null && !meshNature.isEmpty()) {
            return tslDeviceRepository.findByProjectIdAndMeshNature(projectId, meshNature);
        }

        // 如果没传场景，为了兼容性，可以返回该项目所有设备
        return tslDeviceRepository.findAll();
    }

    // =========================================================
    // 2. 获取当前场景下的所有网格
    //    对应前端: fetchGridList -> /api/meshes/all
    // =========================================================
    public List<Map<String, String>> getMeshesByScene(String meshNature) {
        List<Object[]> rawList;

        if (meshNature != null && !meshNature.isEmpty()) {
            rawList = tslDeviceRepository.findDistinctMeshesByScene(meshNature);
        } else {
            // 如果没传场景，返回空或者所有网格
            return Collections.emptyList();
        }

        // 将 Object[] 转换为 Map List，方便前端 JSON 解析
        List<Map<String, String>> result = new ArrayList<>();
        for (Object[] row : rawList) {
            if (row != null && row.length >= 2) {
                Map<String, String> map = new HashMap<>();
                map.put("mesh_no", (String) row[0]);   // 对应前端 meshCode
                map.put("mesh_name", (String) row[1]); // 对应前端 meshName
                result.add(map);
            }
        }
        return result;
    }

    // =========================================================
    // 3. 获取当前场景下的设备类型
    //    对应前端: fetchDeviceTypes -> /api/deviceTypes/fromTslProduct
    // =========================================================
    public List<TslProduct> getProductTypesByScene(String meshNature) {
        if (meshNature != null && !meshNature.isEmpty()) {
            return tslProductRepository.findProductsByMeshNature(meshNature);
        }
        return tslProductRepository.findAll();
    }

    /**
     * 查询设备实例列表
     * [修改] 增加 meshNature 参数，支持过滤
     */
    public Map<String, Object> queryDeviceInstances(String prodId, String meshNature) {
        try {
            // 1. 获取产品指令信息
            String productOps = "无操作指令";
            Optional<TslProduct> productOpt = tslProductRepository.findById(prodId);

            if (productOpt.isPresent()) {
                String instruction = productOpt.get().getProductInstruction();
                if (instruction != null && instruction.startsWith("[")) {
                    productOps = instruction
                            .replace("[", "")
                            .replace("]", "")
                            .replace("\"", "")
                            .replace(",", "，");
                }
            }
            final String finalProductOps = productOps;

            // 2. 根据是否传入场景ID，选择不同的查询方法
            List<TslDevice> deviceList;
            if (meshNature != null && !meshNature.isEmpty()) {
                deviceList = tslDeviceRepository.findByProductProductIdAndMeshNature(prodId, meshNature);
            } else {
                deviceList = tslDeviceRepository.findByProductProductId(prodId);
            }

            // 3. 数据转换 (Entity -> Map) - 保持原有逻辑不变
            List<Map<String, Object>> datas = deviceList.stream().map(device -> {
                Map<String, Object> devMap = new HashMap<>();
                devMap.put("deviceId", String.valueOf(device.getDeviceId()));
                devMap.put("deviceName", device.getDeviceName());
                devMap.put("deviceTypeId", device.getProduct().getProductId());
                devMap.put("deviceRegion", device.getMeshName());
                devMap.put("deviceTime", device.getCreatedAt());

                String statusLabel = "未知";
                Integer status = device.getStatus();
                if (status != null) {
                    statusLabel = (status == 1) ? "离线" : (status == 2) ? "在线" : "未知";
                }
                devMap.put("states", List.of(Map.of("stateKey", "状态", "stateValue", statusLabel)));
                devMap.put("operation", finalProductOps);
                devMap.put("meshCode", device.getMeshNo()); // 补充：前端列表可能用到

                return devMap;
            }).collect(Collectors.toList());

            // 4. 构造返回
            return Map.of(
                    "code", "00000",
                    "success", true,
                    "message", "查询成功",
                    "data", datas
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "查询设备实例失败：" + e.getMessage());
        }
    }

    /**
     * 新增设备实例
     * 保持不变
     */
    @Transactional
    public Map<String, Object> addDeviceInstance(Map<String, String> instanceData) {
        String productId = instanceData.get("deviceTypeId");
        if (productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("设备实例必须关联一个设备类型 (deviceTypeId)。");
        }

        String deviceIdStr = instanceData.get("deviceId");
        String deviceName = instanceData.get("deviceName");

        if (deviceIdStr == null || deviceIdStr.isEmpty() || deviceName == null || deviceName.isEmpty()) {
            throw new IllegalArgumentException("设备序号和设备名称不能为空。");
        }

        TslProduct product = tslProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("无效的产品ID: " + productId));

        String states = instanceData.get("states");
        int status = 2;
        if (states != null) {
            if (states.contains("离线") || states.contains("1")) {
                status = 1;
            } else if (states.contains("在线") || states.contains("2")) {
                status = 2;
            }
        }

        String deviceTime = instanceData.get("deviceTime");
        String createdAt = (deviceTime != null && !deviceTime.isEmpty()) ? deviceTime :
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try {
            TslDevice device = new TslDevice();
            device.setDeviceId(Long.parseLong(deviceIdStr));
            device.setDeviceName(deviceName);
            device.setProduct(product);
            device.setStatus(status);
            device.setMeshName(instanceData.get("deviceRegion"));
            device.setMeshId(instanceData.getOrDefault("meshId", UUID.randomUUID().toString()));
            device.setMeshNo(instanceData.getOrDefault("meshNo", "unknown"));

            // 优先使用前端传来的 meshNature，如果没有则默认为 F-city
            String meshNature = instanceData.get("mesh_nature");
            device.setMeshNature(meshNature != null ? meshNature : "F-city");

            device.setCreatedAt(createdAt);
            device.setProjectId(1001L);

            tslDeviceRepository.save(device);

            return Map.of(
                    "success", true,
                    "message", "设备实例添加成功",
                    "deviceId", deviceIdStr
            );

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("设备ID必须是数字格式");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("设备保存失败: " + e.getMessage());
        }
    }
}