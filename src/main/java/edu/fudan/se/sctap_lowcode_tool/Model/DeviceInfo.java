package edu.fudan.se.sctap_lowcode_tool.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.ArrayList;

/**
 * 设备信息模型类，用于存储设备相关数据。
 */
@Data
public class DeviceInfo {
    private String deviceId;    // 设备的唯一标识符
    private String url;       // 设备的URL，用于远程访问或控制
    private String status;      // 设备的当前状态，例如“在线”、“离线”
    private ArrayList<String> capabilities;     // 设备的能力描述，例如“温度测量”，“音频输出”
    private JsonNode data;        // (需要解析) 设备的数据，例如“当前温度：25℃”
}