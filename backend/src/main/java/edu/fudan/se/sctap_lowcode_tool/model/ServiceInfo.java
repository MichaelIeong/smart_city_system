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
    private Integer id; // 全局唯一标识

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private SpaceInfo parentingSpace;

    @Column(name = "service_id", nullable = false)
    private String serviceId; // 服务ID(由用户自定义, Project内唯一)

    @Column(nullable = false)
    private String serviceName; // 服务名称, 例如“会议模式”

    private String url; // 服务URL(POST请求)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceInfo that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(serviceId, that.serviceId) && Objects.equals(serviceName, that.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, serviceId, serviceName);
    }
}
