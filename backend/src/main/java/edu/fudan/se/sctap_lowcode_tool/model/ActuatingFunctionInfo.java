package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "actuating_functions")
@Data
public class ActuatingFunctionInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;   // 功能的唯一标识符

    private String name;   // 功能的名称

    private String params;   // 功能的参数，以JSON对象格式字符串存储，例如{"mode":"string", "temperature":"int"}

}
