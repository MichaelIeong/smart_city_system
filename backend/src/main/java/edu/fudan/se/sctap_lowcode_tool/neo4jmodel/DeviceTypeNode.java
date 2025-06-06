package edu.fudan.se.sctap_lowcode_tool.neo4jmodel;

import org.springframework.data.neo4j.core.schema.*;
import lombok.Data;
import lombok.ToString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Node("DeviceType")
@Data
public class DeviceTypeNode {

    @Id
    @GeneratedValue
    private Long id;   // 设备类型的唯一标识符

    @Relationship(type = "IN_PROJECT", direction = Relationship.Direction.OUTGOING)
    @ToString.Exclude

    private String deviceTypeId;    // 用户设定的资源ID(Project内唯一)
    private String deviceTypeName;  // 设备类型的名称
    private Boolean isSensor;       // 是否为传感器

    @Relationship(type = "HAS_STATE_TYPE", direction = Relationship.Direction.INCOMING)
    @ToString.Exclude
    private Set<StateDeviceTypeRelation> states = new HashSet<>();   // 状态关系

    @Relationship(type = "SUPPORTED_BY", direction = Relationship.Direction.INCOMING)
    @ToString.Exclude
    private Set<ActuatingFunctionDeviceTypeRelation> actuatingFunctions = new HashSet<>();   // 执行功能关系

    @Relationship(type = "OF_TYPE", direction = Relationship.Direction.INCOMING)
    @ToString.Exclude
    private Set<DeviceNode> devices = new HashSet<>();   // 实例化设备

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceTypeNode that)) return false;
        return Objects.equals(id, that.id) &&
                Objects.equals(deviceTypeId, that.deviceTypeId) &&
                Objects.equals(deviceTypeName, that.deviceTypeName) &&
                Objects.equals(isSensor, that.isSensor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceTypeId, deviceTypeName, isSensor);
    }
}