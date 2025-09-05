package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "services",
        uniqueConstraints = {@UniqueConstraint(
                columnNames = {"service_id"}
        )}
)
@Data
public class ServiceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer serviceId; // 全局唯一标识

    @Column(name = "project_id", nullable = false)
    private String projectId; // 服务所属项目ID (Project 内唯一)

    @Column(name = "service_name", nullable = false)
    private String serviceName; // 服务名称, 例如“会议模式”

    @Column(name = "service_json", columnDefinition = "TEXT")
    private String serviceJson; // 服务组合的 Node-RED JSON

    @Column(name = "service_des", columnDefinition = "TEXT")
    private String description; // 服务描述
}