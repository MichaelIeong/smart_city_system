package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeSummaryDTO;
import edu.fudan.se.sctap_lowcode_tool.model.TslDevice;
import edu.fudan.se.sctap_lowcode_tool.model.TslProduct;
import edu.fudan.se.sctap_lowcode_tool.repository.TslDeviceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.TslProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

                String statusLabel = "运行";
                Integer status = device.getStatus();
                if (status != null) {
                    statusLabel = (status == 1) ? "离线" : (status == 2) ? "在线" : "运行";
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
     */
    @Transactional("jpaTransactionManager")
    public Map<String, Object> addDeviceInstance(Map<String, Object> instanceData) {
        // 1. 获取参数 (前端传过来的是 String)
        String deviceIdStr = (String) instanceData.get("deviceId");
        String deviceName = (String) instanceData.get("deviceName");
        String productTypeId = (String) instanceData.get("deviceTypeId");
        String meshNature = (String) instanceData.get("mesh_nature");

        if (deviceIdStr == null || deviceIdStr.isEmpty() || deviceName == null || deviceName.isEmpty()) {
            throw new IllegalArgumentException("设备序号和设备名称不能为空。");
        }

        // 2. 类型转换 (String -> Long)
        Long deviceId;
        try {
            deviceId = Long.parseLong(deviceIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("设备序号必须是纯数字");
        }

        // 3. 检查重复
        if (tslDeviceRepository.existsByDeviceId(deviceId)) {
            throw new IllegalArgumentException("设备序号 " + deviceId + " 已存在，请勿重复添加。");
        }

        // 4. 查询关联的 Product 对象
        TslProduct product = tslProductRepository.findById(productTypeId)
                .orElseThrow(() -> new IllegalArgumentException("无效的设备类型ID: " + productTypeId));

        // 5. 状态处理
        Object statesObj = instanceData.get("states");
        int status = 2; // 默认为在线
        if (statesObj != null) {
            String s = statesObj.toString();
            if (s.contains("离线") || "1".equals(s)) {
                status = 1;
            }
        }

        // 6. 时间处理
        String deviceTime = (String) instanceData.get("deviceTime");
        String createdAt = (deviceTime != null && !deviceTime.isEmpty()) ? deviceTime :
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // 7. 构建实体
        TslDevice device = new TslDevice();

        device.setDeviceId(deviceId);
        device.setDeviceName(deviceName);

        device.setProduct(product);

        device.setMeshNature(meshNature != null ? meshNature : "F-city");

        String region = (String) instanceData.get("deviceRegion");
        device.setMeshName(region != null ? region : "");

        // 8. 默认值填充
        device.setProjectId(1001L); // Integer 类型
        device.setStatus(status);
        device.setMeshId("");
        device.setMeshNo("");
        device.setAddress("");
        device.setMeshArea(0.0);

        try {
            TslDevice lastDevice = tslDeviceRepository.findTopByOrderByIdDesc();

            int newId = (lastDevice == null) ? 1 : (lastDevice.getId() + 1);

            device.setId(newId);

        } catch (Exception e) {

            device.setId((int) (System.currentTimeMillis() % 100000000));
        }
        tslDeviceRepository.save(device); // 现在 ID 已经有值了，数据库不会再报错

        return Map.of(
                "success", true,
                "message", "设备实例添加成功",
                "deviceId", deviceIdStr
        );
    }

    /**
     * 批量删除设备实例
     * 将前端传来的 String 列表转换为 Long 列表
     */
    @Transactional("jpaTransactionManager")
    public void deleteDeviceInstances(List<String> deviceIdStrs) {
        if (deviceIdStrs == null || deviceIdStrs.isEmpty()) {
            return;
        }

        // 类型转换 List<String> -> List<Long>
        List<Long> deviceIdLongs = deviceIdStrs.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // 调用 Repository
        tslDeviceRepository.deleteByDeviceIdIn(deviceIdLongs);
    }
}