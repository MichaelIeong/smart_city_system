package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "DeviceHistory")
@Data
public class DeviceHistory {
    @Id
    @Column(name = "history_id", nullable = false)
    private String historyId;   // 历史记录的唯一标识符，应有其自己的ID

//    @ManyToOne
//    @JoinColumn(name = "device_id", nullable = false) // 这里使用JoinColumn来指定外键
//    private DeviceInfo device;   // 对应的设备，建立多对一的关系
}