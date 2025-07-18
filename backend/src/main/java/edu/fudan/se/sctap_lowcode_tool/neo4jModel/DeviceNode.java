package edu.fudan.se.sctap_lowcode_tool.neo4jModel;

import lombok.Data;
import lombok.ToString;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.data.neo4j.core.schema.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Node("Device")
@Data
public class DeviceNode {

    @Id
    @Property("deviceId")
    private String deviceId;     // 自定义设备ID
    @ToString.Exclude
    @Relationship(type = "INSTALLED_IN", direction = Relationship.Direction.OUTGOING)
    private SpaceNode space;   // 所属空间

    private String deviceName;   // 设备名称
    private String fixedProperties; // 固定属性（JSON）

    private Float coordinateX;
    private Float coordinateY;
    private Float coordinateZ;

    private LocalDateTime lastUpdateTime;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
    private DeviceTypeNode deviceType;

    @Relationship(type = "HAS_STATE", direction = Relationship.Direction.INCOMING)
    @ToString.Exclude
    private Set<StateDeviceRelation> states;

    @Relationship(type = "HAS_FUNCTION", direction = Relationship.Direction.OUTGOING)
    @ToString.Exclude
    private Set<ActuatingFunctionDeviceRelation> actuatingFunctions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceNode that)) return false;
        return Objects.equals(deviceId, that.deviceId)
                && Objects.equals(deviceName, that.deviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, deviceName);
    }
}