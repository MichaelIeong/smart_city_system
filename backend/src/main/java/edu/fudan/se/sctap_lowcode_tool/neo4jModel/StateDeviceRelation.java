package edu.fudan.se.sctap_lowcode_tool.neo4jModel;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.neo4j.core.schema.*;

@RelationshipProperties
@Data
public class StateDeviceRelation {

    @Id
    @GeneratedValue
    private Long id;

    @Relationship(type = "HAS_STATE", direction = Relationship.Direction.INCOMING)
    @ToString.Exclude
    private DeviceNode device;

    private String stateValue;  // 状态值，如 "开"、"关闭"、"亮度：60%"

    @TargetNode
    private StateNode state;
}