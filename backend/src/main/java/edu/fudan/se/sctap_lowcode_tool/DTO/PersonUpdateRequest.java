package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

/**
 * 用於更新人員的請求資料
 */
@Data
public class PersonUpdateRequest {
    private String personName;    // 可選：新的人員名稱
    private String spaceId;      // 可選：新空間 ID，可為 null 表示離開空間
}