package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "device_types")
@Data
public class DeviceTypeInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int typeId;   // 设备类型的唯一标识符

    @Column(nullable = false)
    private String deviceTypeName;   // 设备类型的名称

    @Column(nullable = false)
    private Boolean isSensor;   // 设备类型的名称

    @Column
    private String capabilities; // 设备的能力描述，表示能返回什么data，例如“温度float(sensor)”，“音频输出(device)”

    @OneToMany(mappedBy = "deviceType", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<DeviceInfo> devices = new HashSet<>();   // 设备实例

}