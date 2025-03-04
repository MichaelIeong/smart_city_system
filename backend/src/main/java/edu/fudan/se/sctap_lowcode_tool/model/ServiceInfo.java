package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.Objects;

@Entity
@Table(name = "services",
        uniqueConstraints = {@UniqueConstraint(
                columnNames = {"space_id", "service_id"}
        )}
)
@Data
public class ServiceInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer serviceId; // 全局唯一标识

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private SpaceInfo parentingSpace;


    @Column(name = "project_id", nullable = false)
    private String projectId; // 服务ID(由用户自定义, Project内唯一)

    @Column(name = "service_name", nullable = false)
    private String serviceName; // 服务名称, 例如“会议模式”

    @Column(name = "service_json", columnDefinition = "TEXT")
    private String serviceJson; // 服务组合的node-red的json

    @Column(name = "service_csp", nullable = true)
    private String serviceCsp; // 服务的CSP模型


}
