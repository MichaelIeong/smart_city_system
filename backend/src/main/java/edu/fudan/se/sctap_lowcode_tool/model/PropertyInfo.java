package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "properties",
        uniqueConstraints = {@UniqueConstraint(
                columnNames = {"project_id", "property_id"}
        )}
)
@Data
public class PropertyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;   // 属性的唯一标识符

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectInfo projectInfo;   // 属性所属的Project

    @Column(name = "property_id", nullable = false)
    private String propertyId; // 用户设定的属性ID(Project内唯一)

    @Column(nullable = false)
    private String propertyKey;   // 属性的名称

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<PropertySpace> parentingSpaces = new HashSet<>();   // 属性所属的空间

}
