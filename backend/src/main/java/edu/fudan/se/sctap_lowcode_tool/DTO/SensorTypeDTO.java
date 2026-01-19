package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SensorTypeDTO {

    private String productId;
    private String productName;

    /**
     * 已解析的事件列表
     */
    private List<String> sensingEvents;
}