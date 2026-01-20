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

    @Column(name = "description")
    private String description;   // 资源描述

    @Column(name = "details")
    private String details;

    @Column(name = "input")
    private String cyberResourceJson;

    private String state;  // 资源状态

    private LocalDateTime lastUpdateTime;  // 资源最后更新时间

    @Column(name = "url", nullable = false, length = 512)
    private String url;  // 资源的访问链接或 API 地址

    @Column(name = "output", columnDefinition = "TEXT")
    private String output;

    public String getInput() {
        return this.cyberResourceJson;
    }

    public void setInput(String input) {
        this.cyberResourceJson = input;
    }
    public void setProjectId(Integer projectId) {
        if (projectId != null) {
            ProjectInfo p = new ProjectInfo();
            p.setProjectId(projectId);
            this.projectInfo = p;
        }
    }

}
