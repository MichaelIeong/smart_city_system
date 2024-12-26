package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "device_types")
@Data
public class DeviceTypeInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;   // 设备类型的唯一标识符

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectInfo projectInfo;   // 所属Project

    @Column(nullable = false)
    private String deviceTypeName;   // 设备类型的名称

    @Column(nullable = false)
    private Boolean isSensor;   // 是否为传感器

    @OneToMany(mappedBy = "deviceType", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<StateDeviceType> states = new HashSet<>();   // 设备类型的状态

    @OneToMany(mappedBy = "deviceType", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<ActuatingFunctionDeviceType> actuatingFunctions = new HashSet<>();   // 设备类型的执行功能

    @OneToMany(mappedBy = "deviceType", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<DeviceInfo> devices = new HashSet<>();   // 设备实例

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceTypeInfo that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(deviceTypeName, that.deviceTypeName) && Objects.equals(isSensor, that.isSensor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceTypeName, isSensor);
    }
}