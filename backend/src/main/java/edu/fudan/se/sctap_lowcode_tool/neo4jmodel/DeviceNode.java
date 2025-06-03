package edu.fudan.se.sctap_lowcode_tool.neo4jmodel;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.neo4j.core.schema.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Node("Device")
@Data
public class DeviceNode {

    @Id
    @GeneratedValue
    private Long id;   // 设备的唯一标识符

    @ToString.Exclude
    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
    private SpaceNode space;   // 所属空间

    private String deviceId;     // 自定义设备ID（空间内唯一）
    private String deviceName;   // 设备名称
    private String fixedProperties; // 固定属性（JSON）

    private Float coordinateX;
    private Float coordinateY;
    private Float coordinateZ;

    private LocalDateTime lastUpdateTime;

    @Relationship(type = "OF_TYPE", direction = Relationship.Direction.OUTGOING)
    private DeviceTypeNode deviceType;

    @Relationship(type = "HAS_STATE", direction = Relationship.Direction.INCOMING)
    @ToString.Exclude
    private Set<StateDeviceRelation> states;

    @Relationship(type = "CONTROLLED_BY", direction = Relationship.Direction.INCOMING)
    @ToString.Exclude
    private Set<ActuatingFunctionDeviceRelation> actuatingFunctions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceNode that)) return false;
        return Objects.equals(id, that.id)
                && Objects.equals(deviceId, that.deviceId)
                && Objects.equals(deviceName, that.deviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceId, deviceName);
    }
}