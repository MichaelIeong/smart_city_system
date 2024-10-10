package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

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
    private Integer id; // 全局唯一标识

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private SpaceInfo parentingSpace;

    @Column(name = "service_id", nullable = false)
    private String serviceId; // 服务ID(由用户自定义, Project内唯一)

    @Column(nullable = false)
    private String serviceName; // 服务名称, 例如“会议模式”

}
