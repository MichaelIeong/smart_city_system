package edu.fudan.se.sctap_lowcode_tool.neo4jmodel;

import org.springframework.data.neo4j.core.schema.*;
import lombok.Data;

@Node("ActuatingFunction")
@Data
public class ActuatingFunctionNode {

    @Id
    @GeneratedValue
    private Long id;   // 功能的唯一标识符

    private String name;         // 功能名称
    private String params;       // 功能参数（JSON字符串）
    private String description;  // 功能描述
}