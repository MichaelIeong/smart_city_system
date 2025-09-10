package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

import java.util.List;

@Data
public class DeviceCreateRequest {
    private Integer deviceId;          // 可选：如果你让后端生成，就不传
    private String deviceName;
    private String description;

    private Integer spaceId;           // 下拉选择返回的主键
    private Integer deviceTypeId;      // 下拉选择返回的主键

    private List<FunctionBinding> functions; // 设备-功能的带属性关系
    @Data
    public static class FunctionBinding {
        private Integer actuatingFunctionId; // 功能主键
        private String url;                  // 设备级控制端点（在关系上）
        private String description;          // 可选
    }
}
