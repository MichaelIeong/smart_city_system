package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "DeviceHistory")
@Data
public class DeviceHistory {
    @Id
    @Column(name = "deviceId", nullable = false)
    private String deviceId;   // 设备的唯一标识符
}
