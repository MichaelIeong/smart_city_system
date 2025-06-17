package edu.fudan.se.sctap_lowcode_tool.neo4jModel;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.neo4j.core.schema.*;

@RelationshipProperties
@Data
public class StateDeviceTypeRelation {

    @Id
    @GeneratedValue
    private Long id;

    @Relationship(type = "SUPPORTS_STATE", direction = Relationship.Direction.INCOMING)
    @ToString.Exclude
    private DeviceTypeNode deviceType;

    @TargetNode
    private StateNode state;
}