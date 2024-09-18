package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;


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
    @Column
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

    @Column
    private String url;   // 设备的URL，用于远程访问或控制

    @Column
    private String status;  // 设备的当前状态，例如“在线”、“离线”

    @Column
    private String data; // 设备的数据，例如“当前温度：25℃”

    @Column
    private float coordinateX; // 设备的横坐标

    @Column
    private float coordinateY; // 设备的纵坐标

    @Column
    private float coordinateZ; // 设备的Z轴坐标
}