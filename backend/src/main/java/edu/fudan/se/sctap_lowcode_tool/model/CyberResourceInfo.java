package edu.fudan.se.sctap_lowcode_tool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "cyber_resources",
        uniqueConstraints = {@UniqueConstraint(
                columnNames = {"project_id", "resource_id"}
        )}
)
@Data
public class CyberResourceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;   // 社会资源唯一标识符

    @ToString.Exclude
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectInfo projectInfo;   // 资源所属的Project

    @Column(name = "resource_id", nullable = false)
    private String resourceId; // 用户设定的资源ID(Project内唯一)

    private String resourceType;   // 资源类型

    private String description;   // 资源描述

    private String details;

    private String state;  // 资源状态

    private LocalDateTime lastUpdateTime;  // 资源最后更新时间

}
