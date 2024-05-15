package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "device_info")
@Data
public class DeviceInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "device_id", nullable = false)
    private int deviceId;   // 设备的唯一标识符

    @Column(name = "space_id", columnDefinition = "int default 0", nullable = false)
    private int spaceId; // 设备所在空间的ID

    @Column(name = "name", nullable = false)
    private String name;   // 设备的名称

    @Column(name = "url", nullable = false)
    private String url;   // 设备的URL，用于远程访问或控制

    @Column(name = "status", nullable = true)
    private String status;  // 设备的当前状态，例如“在线”、“离线”

    @Column(name = "capabilities")
    private String capabilities; // 设备的能力描述，例如“温度测量”，“音频输出”

    @Column(name = "data")
    private String data; // 设备的数据，例如“当前温度：25℃”

//    // 多对一的表，一个设备对应多个设备历史记录。使用懒加载，只有在真正访问这些历史记录时，它们才会从数据库中加载。
//    // 且持久化操作（如保存、更新、删除等）都会从 DeviceInfo 级联到其关联的 DeviceHistory
//    @OneToMany(mappedBy = "device", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private Set<DeviceHistory> deviceHistories;
}