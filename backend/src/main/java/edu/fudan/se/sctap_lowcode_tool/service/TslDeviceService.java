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
}