package edu.fudan.se.sctap_lowcode_tool.neo4jmodel;

import org.springframework.data.neo4j.core.schema.*;
import lombok.Data;

@Node("State")
@Data
public class StateNode {

    @Id
    @GeneratedValue
    private Long id;   // Neo4j内部主键

    private String stateKey;   // 状态名称或标识符（如“电源状态”、“亮度等级”等）
}