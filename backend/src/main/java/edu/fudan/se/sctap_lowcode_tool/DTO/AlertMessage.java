package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AlertMessage {
    // 消息类型：event app
    private String type;
    // 发生位置网格id
    private String location;
    // 发生时间戳
    private LocalDateTime timestamp;
    // 附加信息
    private Map<String, Object> data;
}
