package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.Objects;
import java.util.Set;


@Entity
@Table(name = "devices",
        uniqueConstraints = {@UniqueConstraint(
                columnNames = {"space_id", "device_id"}
        )}
)
@Data
public class DeviceInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;   // 设备的唯一标识符

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private SpaceInfo space;   // 设备所属的空间

    @Column(name = "device_id", nullable = false)
    private String deviceId; // 用户设定的设备ID(Space内唯一)

    @Column(nullable = false)
    private String deviceName;   // 设备的名称

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_type_id")
    private DeviceTypeInfo deviceType;   // 设备的类型

    @OneToMany(mappedBy = "device", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<StateDevice> states;   // 设备的状态

    @OneToMany(mappedBy = "device", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ActuatingFunctionDevice> actuatingFunctions;   // 设备的执行功能

    private String fixedProperties; // 设备的固定属性，以JSON对象格式字符串存储，例如{"color":"red", "protocol":"zigbee"}

    private float coordinateX; // 设备的横坐标

    private float coordinateY; // 设备的纵坐标

    private float coordinateZ; // 设备的Z轴坐标

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceInfo that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(deviceId, that.deviceId) && Objects.equals(deviceName, that.deviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceId, deviceName);
    }
}