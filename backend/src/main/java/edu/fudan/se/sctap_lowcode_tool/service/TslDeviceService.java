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

    /**
     * 查询设备实例列表
     * 重构说明：保持原有 Map 返回结构，适配前端需求
     */
    public Map<String, Object> queryDeviceInstances(String prodId) {
        try {
            // 1. 获取产品指令信息 (对应原 JDBC 查询 tsl_product)
            String productOps = "无操作指令";
            Optional<TslProduct> productOpt = tslProductRepository.findById(prodId);

            if (productOpt.isPresent()) {
                String instruction = productOpt.get().getProductInstruction(); // 假设 Entity 有此 getter
                if (instruction != null && instruction.startsWith("[")) {
                    // 保持原有的字符串清洗逻辑
                    productOps = instruction
                            .replace("[", "")
                            .replace("]", "")
                            .replace("\"", "")
                            .replace(",", "，");
                }
            }
            final String finalProductOps = productOps;

            // 2. 查询设备列表 (对应原 JDBC 查询 tsl_devices)
            List<TslDevice> deviceList = tslDeviceRepository.findByProductProductId(prodId);

            // 3. 数据转换 (Entity -> 前端需要的 Map 结构)
            List<Map<String, Object>> datas = deviceList.stream().map(device -> {
                Map<String, Object> devMap = new HashMap<>();

                // 基础字段映射
                devMap.put("deviceId", String.valueOf(device.getDeviceId())); // Long 转 String 适配前端
                devMap.put("deviceName", device.getDeviceName());
                devMap.put("deviceTypeId", device.getProduct().getProductId());
                devMap.put("deviceRegion", device.getMeshName());
                devMap.put("deviceTime", device.getCreatedAt());

                // 状态转换逻辑: int -> String
                String statusLabel = "未知";
                Integer status = device.getStatus();
                if (status != null) {
                    statusLabel = (status == 1) ? "离线" : (status == 2) ? "在线" : "未知";
                }
                // 保持原有的嵌套结构
                devMap.put("states", List.of(Map.of("stateKey", "状态", "stateValue", statusLabel)));

                // 操作指令
                devMap.put("operation", finalProductOps);

                return devMap;
            }).collect(Collectors.toList());

            // 4. 构造标准返回结果
            return Map.of(
                    "code", "00000",
                    "success", true,
                    "message", "从数据库查询成功 (JPA)",
                    "data", datas
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "查询设备实例失败：" + e.getMessage());
        }
    }

    /**
     * 新增设备实例
     */
    @Transactional
    public Map<String, Object> addDeviceInstance(Map<String, String> instanceData) {
        // 1. 参数校验
        String productId = instanceData.get("deviceTypeId"); // 前端传的是 deviceTypeId
        if (productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("设备实例必须关联一个设备类型 (deviceTypeId)。");
        }

        String deviceIdStr = instanceData.get("deviceId");
        String deviceName = instanceData.get("deviceName");

        if (deviceIdStr == null || deviceIdStr.isEmpty() || deviceName == null || deviceName.isEmpty()) {
            throw new IllegalArgumentException("设备序号和设备名称不能为空。");
        }

        // 2. 查找关联产品 (外键约束)
        TslProduct product = tslProductRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("无效的产品ID: " + productId));

        // 3. 状态转换 logic (String -> int)
        String states = instanceData.get("states");
        int status = 2; // 默认在线
        if (states != null) {
            if (states.contains("离线") || states.contains("1")) {
                status = 1;
            } else if (states.contains("在线") || states.contains("2")) {
                status = 2;
            }
        }

        // 4. 时间处理
        String deviceTime = instanceData.get("deviceTime");
        String createdAt = (deviceTime != null && !deviceTime.isEmpty()) ? deviceTime :
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try {
            // 5. 构建并保存实体
            TslDevice device = new TslDevice();
            device.setDeviceId(Long.parseLong(deviceIdStr)); // 将 String 转为 Long
            device.setDeviceName(deviceName);
            device.setProduct(product); // 设置关联关系
            device.setStatus(status);
            device.setMeshName(instanceData.get("deviceRegion")); // 映射 region -> meshName
            device.setMeshId(instanceData.getOrDefault("meshId", UUID.randomUUID().toString())); // 随机生成防止非空报错
            device.setMeshNo(instanceData.getOrDefault("meshNo", "unknown"));
            device.setMeshNature("F-city"); // 默认值，或者从前端获取
            device.setCreatedAt(createdAt);
            device.setProjectId(1001L); // 默认项目ID

            tslDeviceRepository.save(device);

            return Map.of(
                    "success", true,
                    "message", "设备实例添加成功 (JPA)",
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