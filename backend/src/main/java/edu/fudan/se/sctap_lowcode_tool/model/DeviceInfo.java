package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "devices")
@Data
public class DeviceInfo {
    @Id
    @Column
    private String deviceId;   // 设备的唯一标识符

    @Column(nullable = false)
    private String deviceName;   // 设备的名称

    @Column//(nullable = false)
    private String url;   // 设备的URL，用于远程访问或控制

    @Column
    private String status;  // 设备的当前状态，例如“在线”、“离线”

    @Column
    private String type;    // 设备的类型，例如“温度传感器”、“灯”

    @Column
    private Boolean isSensor;  // 设备是否为传感器

    @Column
    private String capabilities; // 设备的能力描述，表示能返回什么data，例如“温度float(sensor)”，“音频输出(device)”

    @Column
    private String data; // 设备的数据，例如“当前温度：25℃”

    @Column
    private float coordinateX; // 设备的横坐标

    @Column
    private float coordinateY; // 设备的纵坐标

    @Column
    private float coordinateZ; // 设备的Z轴坐标
}