package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

@Data
public class PersonCreateRequest {
    private String personName;
    private String spaceId; // 可為 null 表示無所屬空間
}