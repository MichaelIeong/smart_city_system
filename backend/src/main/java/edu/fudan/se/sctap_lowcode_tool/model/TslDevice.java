package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tsl_devices")
public class TslDevice {

    @Id
    @Column(name = "id")
    private Integer id;

    /**
     * 项目 ID (对应 project_id)
     */
    @Column(name = "project_id")
    private Long projectId;

    /**
     * 设备名称 (对应 device_name)
     * Service 调用: device.setDeviceName(...)
     */
    @Column(name = "device_name")
    private String deviceName;

    /**
     * 设备逻辑编号 (对应 device_id)
     * 注意：这是业务上的 ID（如 2025112500500），不同于主键 id
     * Service 调用: device.setDeviceId(...)
     */
    @Column(name = "device_id")
    private Long deviceId;

    /**
     * 关联的产品 (对应 product_id)
     * 多对一关系
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    private TslProduct product;

    /**
     * 状态 (对应 status)
     * 1:离线, 2:在线
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 网格 UUID (对应 mesh_id)
     */
    @Column(name = "mesh_id")
    private String meshId;

    /**
     * 网格编号 (对应 mesh_no，如 f-city-1)
     * Service 调用: device.setMeshNo(...)
     */
    @Column(name = "mesh_no")
    private String meshNo;

    /**
     * 网格名称 (对应 mesh_name，如 永德城区01网格)
     */
    @Column(name = "mesh_name")
    private String meshName;

    /**
     * 网格性质/场景类型 (对应 mesh_nature，如 F-city)
     */
    @Column(name = "mesh_nature")
    private String meshNature;

    /**
     * 网格面积 (对应 mesh_area)
     */
    @Column(name = "mesh_area")
    private Double meshArea;

    /**
     * 地址 (对应 address)
     */
    @Column(name = "address")
    private String address;

    /**
     * 创建时间 (对应 created_at)
     * 为了简化处理，这里映射为 String。如果数据库是 datetime 类型，JPA 也能自动转换。
     */
    @Column(name = "created_at")
    private String createdAt;
}