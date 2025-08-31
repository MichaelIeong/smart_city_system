package edu.fudan.se.sctap_lowcode_tool.neo4jModel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("Device")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DeviceNode {

    @Id
    @EqualsAndHashCode.Include
    private Integer deviceId;

    private String deviceName;

    private String description;

    /**
     * Device —LOCATED_IN→ Space
     * 表示该设备位于哪个空间
     */
    @Relationship(type = "LOCATED_IN", direction = Relationship.Direction.OUTGOING)
    private SpaceNode locatedIn;

    /**
     * Device —OF_TYPE→ DeviceType
     * 表示该设备属于哪个设备类型
     */
    @Relationship(type = "OF_TYPE", direction = Relationship.Direction.OUTGOING)
    private DeviceTypeNode deviceType;

    /**
     * Device —HAS_ACTUATING_FUNCTION→ ActuatingFunction
     * 这是带属性关系（ActuatingFunctionDeviceRelation），依然保留 url 等字段
     */
    @Relationship(type = "HAS_ACTUATING_FUNCTION", direction = Relationship.Direction.OUTGOING)
    private Set<ActuatingFunctionDeviceRelation> actuatingFunctions = new HashSet<>();

    // ====== 便捷方法 ======
    public void addActuatingFunction(ActuatingFunctionDeviceRelation relation) {
        if (relation == null) return;
        if (actuatingFunctions == null) actuatingFunctions = new HashSet<>();
        actuatingFunctions.add(relation);
    }

    public void removeActuatingFunction(ActuatingFunctionDeviceRelation relation) {
        if (relation == null) return;
        if (actuatingFunctions != null) actuatingFunctions.remove(relation);
    }
}