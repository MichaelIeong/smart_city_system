package edu.fudan.se.sctap_lowcode_tool.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(
        name = "spaces",
        uniqueConstraints = {@UniqueConstraint(
                columnNames = {"project_id", "space_id"}
        )}
)
@Data
public class SpaceInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;   // 空间的唯一标识符(全局唯一)

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectInfo projectInfo;   // 空间所属的Project

    @Column(name = "space_id", nullable = false)
    private String spaceId; // 用户设定的空间ID(Project内唯一)

    @Column(nullable = false)
    private String spaceName;   // 空间的名称

    private String fixedProperties;   // 空间的固定属性，用JSON格式存储，例如"{ "面积"："100平方米", "车位总数": "100" }"

    private String description;   // 空间的描述，例如“卧室1”

    @OneToMany(mappedBy = "space", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<PropertySpace> properties = new HashSet<>();   // 空间的属性

    @JsonManagedReference
    @OneToMany(mappedBy = "space", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<DeviceInfo> spaceDevices = new HashSet<>();  // 空间内的设备

    @OneToMany(mappedBy = "parentingSpace", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<EventInfo> events = new HashSet<>();   // 空间的事件

    @OneToMany(mappedBy = "parentingSpace", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ServiceInfo> services = new HashSet<>();   // 空间的服务

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "adjacent_space",
            joinColumns = @JoinColumn(name = "space_id"),
            inverseJoinColumns = @JoinColumn(name = "adjacent_space_id")
    )
    private Set<SpaceInfo> adjacentSpaces = new HashSet<>();   // 相邻的空间

    public void addAdjacentSpace(SpaceInfo space) {
        if (this.id.equals(space.id) || this.adjacentSpaces.contains(space)) {
            return;
        }
        adjacentSpaces.add(space);
        space.getAdjacentSpaces().add(this);
    }

    public void removeAdjacentSpace(SpaceInfo space) {
        if (this.id.equals(space.id) || !this.adjacentSpaces.contains(space)) {
            return;
        }
        adjacentSpaces.remove(space);
        space.getAdjacentSpaces().remove(this);
    }
}