package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // 生成全参构造函数，用于 JPQL 的 new 语法
@NoArgsConstructor
public class DeviceTypeSummaryDTO {
    private String name;
    private String info;
    private Long count; // 注意：JPA 的 count() 返回 Long，建议这里改为 Long
}
