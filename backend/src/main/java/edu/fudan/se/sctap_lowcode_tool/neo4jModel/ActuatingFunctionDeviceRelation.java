package edu.fudan.se.sctap_lowcode_tool.neo4jModel;




import lombok.Data;
import org.springframework.data.neo4j.core.schema.*;

@RelationshipProperties
@Data
public class ActuatingFunctionDeviceRelation {

    @Id
    @GeneratedValue
    private Long id;

    @Relationship(type = "HAS_FUNCTION", direction = Relationship.Direction.INCOMING)
    private ActuatingFunctionNode actuatingFunction;

    private String url;          // 控制该设备的 URL
    private String description;  // 控制说明或备注信息

    @TargetNode
    private DeviceNode device;
}