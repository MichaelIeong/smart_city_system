package edu.fudan.se.sctap_lowcode_tool.neo4jModel;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

/**
 * Space —HAS_ACTUATING_FUNCTION→ ActuatingFunction
 * 注意：这里不包含 url，因为 url 是设备级的属性，
 * 继续保留在 ActuatingFunctionDeviceRelation 中。
 */
@RelationshipProperties
@Data
public class ActuatingFunctionSpaceRelation {

    @Id
    @GeneratedValue
    private Long id;

    @TargetNode
    private ActuatingFunctionNode actuatingFunction;

    /** 可选：空间层面的一些说明信息 */
    private String description;
}