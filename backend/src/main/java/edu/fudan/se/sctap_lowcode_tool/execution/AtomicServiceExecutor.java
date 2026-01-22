package edu.fudan.se.sctap_lowcode_tool.execution;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 原子服务执行器：负责执行工作流中最小单元的具体业务逻辑
 */
@Component
public class AtomicServiceExecutor {
    private String formatLog(String level, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss"));
        return String.format("[%s]-[%s]: %s", level, timestamp, message);
    }
    public String executeCyber(JsonNode stepNode, Map<String, Object> finalArgs) {
        try {
            String serviceName = stepNode.path("name").asText();     
            // 返回格式化后的成功日志
            return formatLog("INFO", "成功调用网络服务: " + serviceName);
        } catch (Exception e) {
            // 返回格式化后的错误日志
            return formatLog("ERROR", "网络服务调用异常: " + e.getMessage());
        }
    }

    public String executePhysical(JsonNode stepNode, Integer areaId, Map<String, Object> finalArgs) {
        try {
            String deviceName = stepNode.path("name").asText();
            // ... 这里的逻辑 ...
            return formatLog("INFO", "成功操作物理设备: " + deviceName);
        } catch (Exception e) {
            return formatLog("ERROR", "物理设备操作失败: " + e.getMessage());
        }
    }

    // executeSocial 同理...
    public String executeSocial(JsonNode stepNode, Map<String, Object> finalArgs) {
        try {
            String serviceName = stepNode.path("name").asText();
            // ... 这里的逻辑 ...
            return formatLog("INFO", "成功调用社会资源: " + serviceName);
        } catch (Exception e) {
            return formatLog("ERROR", "社会资源调用失败: " + e.getMessage());
        }
    }
}