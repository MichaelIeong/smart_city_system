package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "projects")
@Data
public class ProjectInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer projectId;   // 项目的唯一标识符

    @Column(nullable = false)
    private String projectName;   // 项目的名称

    @Column
    private String projectImage;   // 项目的图片

    @OneToMany(mappedBy = "projectInfo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<SpaceInfo> spaces = new HashSet<>();   // 项目中包含的空间

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectInfo that)) return false;
        return Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(projectId);
    }
}
