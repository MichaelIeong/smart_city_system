package edu.fudan.se.sctap_lowcode_tool.neo4jmodel;


import lombok.Data;
import lombok.ToString;
import org.springframework.data.neo4j.core.schema.*;

@RelationshipProperties
@Data
public class ActuatingFunctionDeviceTypeRelation {

    @Id
    @GeneratedValue
    private Long id;

    @Relationship(type = "SUPPORTS_TYPE", direction = Relationship.Direction.INCOMING)
    @ToString.Exclude
    private ActuatingFunctionNode actuatingFunction;

    @TargetNode
    @ToString.Exclude
    private DeviceTypeNode deviceType;
}