package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Service
public class TslDeviceService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 移除所有外部接口相关私有方法

    /**
     * 查询设备实例列表（从本地数据库 tsl_devices 获取）
     */
    public Map<String, Object> queryDeviceInstances(String prodId) {
        try {
            // 1. 查询 tsl_product 中的指令信息 (保留原逻辑)
            String productInstruction = "";
            String productOps = "无操作指令";
            try {
                String productSql = "SELECT product_instruction FROM tsl_product WHERE product_id = ?";
                productInstruction = jdbcTemplate.queryForObject(productSql, String.class, prodId);

                if (productInstruction != null && productInstruction.startsWith("[")) {
                    // 格式化指令字符串，例如: ["cmd1", "cmd2"] -> "cmd1，cmd2"
                    productOps = productInstruction
                            .replace("[", "")
                            .replace("]", "")
                            .replace("\"", "")
                            .replace(",", "，");
                }
            } catch (Exception e) {
                // 忽略找不到指令的异常，使用默认值
            }
            final String finalProductOps = productOps;

            // 2. 从 tsl_devices 表中查询设备实例列表
            // 【关键修正】：FROM tsl_devices
            String instanceSql = "SELECT device_id, device_name, product_id, status, mesh_name, created_at " +
                    "FROM tsl_devices " +
                    "WHERE product_id = ?";

            List<Map<String, Object>> datas = jdbcTemplate.query(instanceSql, (rs, rowNum) -> {
                Map<String, Object> devMap = new HashMap<>();

                // 原始数据
                String deviceId = rs.getString("device_id");
                String deviceName = rs.getString("device_name");
                String statusValue = String.valueOf(rs.getInt("status"));
                String meshName = rs.getString("mesh_name");
                String createdAtString = rs.getString("created_at");

                // === 格式化和转换逻辑 ===

                // 状态转换：1:离线, 2:在线
                String statusLabel = "未知";
                try {
                    int s = Integer.parseInt(statusValue);
                    statusLabel = (s == 1) ? "离线" : (s == 2) ? "在线" : "未知";
                } catch (Exception ignored) {}

                // 时间格式化：直接使用数据库中的字符串
                String deviceTime = createdAtString;

                // 构造前端需要的 Map 格式
                devMap.put("deviceId", deviceId);
                devMap.put("deviceName", deviceName);
                devMap.put("deviceTypeId", rs.getString("product_id"));
                devMap.put("deviceRegion", meshName);
                // 状态字段：使用 List<Map> 格式
                devMap.put("states", List.of(Map.of("stateKey", "状态", "stateValue", statusLabel)));
                devMap.put("deviceTime", deviceTime); // 返回给前端的字段名
                devMap.put("operation", finalProductOps);

                return devMap;
            }, prodId);

            // 3. 构造返回结果 (模拟原接口成功的返回格式)
            return Map.of(
                    "code", "00000",
                    "success", true,
                    "message", "从本地数据库查询成功",
                    "data", datas
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "本地数据库查询设备实例失败：" + e.getMessage());
        }
    }
    // TslDeviceService.java (在现有代码中添加以下方法)

    /**
     * 新增设备实例到 tsl_devices 表
     * @param instanceData 包含 deviceId, deviceName, deviceRegion, deviceTime, states, operation, deviceTypeId
     * @return 成功信息
     */
    public Map<String, Object> addDeviceInstance(Map<String, String> instanceData) {
        String productId = instanceData.get("deviceTypeId");
        if (productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("设备实例必须关联一个设备类型 (deviceTypeId)。");
        }

        String deviceId = instanceData.get("deviceId");
        String deviceName = instanceData.get("deviceName");
        String deviceRegion = instanceData.get("deviceRegion"); // 映射到 mesh_name
        String deviceTime = instanceData.get("deviceTime"); // 映射到 created_at
        String states = instanceData.get("states");

        if (deviceId == null || deviceId.isEmpty() || deviceName == null || deviceName.isEmpty()) {
            throw new IllegalArgumentException("设备序号和设备名称不能为空。");
        }

        // 状态转换：前端输入可能是文字，我们将其转换为数据库的 int (1:离线, 2:在线)
        int status = 2; // 默认假设新增设备为 '在线'
        if (states != null) {
            if (states.contains("离线") || states.contains("1")) {
                status = 1;
            } else if (states.contains("在线") || states.contains("2")) {
                status = 2;
            }
        }

        // created_at 字段：使用当前时间或前端传入的值
        String createdAt = deviceTime != null && !deviceTime.isEmpty() ? deviceTime :
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        // project_id 假设使用默认值 1001 (与您的 CSV 文件中数据一致)
        String projectId = "1001";

        String sql = "INSERT INTO tsl_devices (device_id, device_name, product_id, status, mesh_name, created_at, project_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        int updated = jdbcTemplate.update(sql,
                deviceId,
                deviceName,
                productId,
                status,
                deviceRegion, // mesh_name
                createdAt,
                projectId);

        if (updated > 0) {
            return Map.of("success", true, "message", "设备实例添加成功", "deviceId", deviceId);
        } else {
            throw new RuntimeException("设备实例添加失败，数据库未更新。");
        }
    }
}
