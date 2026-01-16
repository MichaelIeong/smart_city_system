package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "tsl_devices")
@Data
public class Tsldevices {

    @Id
    @Column(name = "product_id")
    private String productId;

    @Column(name = "mesh_id")
    private String meshId;

    @Column(name = "device_name")
    private String deviceName; // 存储 JSON 数组或单个字符串

    @Column(name = "mesh_name")
    private String meshName;
}
