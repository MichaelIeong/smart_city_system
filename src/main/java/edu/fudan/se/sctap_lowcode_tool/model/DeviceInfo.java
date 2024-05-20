package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "devices")
@Data
public class DeviceInfo {
    @Id
    @GeneratedValue
    @Column
    private int deviceId;   // 设备的唯一标识符

    @Column(nullable = false)
    private int spaceId; // 设备所在空间的ID

    @Column(nullable = false)
    private String deviceName;   // 设备的名称

    @Column(nullable = false)
    private String url;   // 设备的URL，用于远程访问或控制

    @Column
    private String status;  // 设备的当前状态，例如“在线”、“离线”

    @Column
    private String capabilities; // 设备的能力描述，例如“温度测量”，“音频输出”

    @Column
    private String data; // 设备的数据，例如“当前温度：25℃”
}