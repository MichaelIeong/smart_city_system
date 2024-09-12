package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "spaces")
@Data
public class SpaceInfo {
    @Id
    @Column
    private String spaceId;   // 空间的唯一标识符

    @Column(nullable = false)
    private String spaceName;   // 空间的名称

    private String type;   // 空间的类型，例如“卧室”、“客厅”

    private String description;   // 空间的描述，例如“卧室1”

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId")
    private ProjectInfo projectInfo;   // 空间所属的场景

    @OneToMany(mappedBy = "space", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<PropertyInfo> properties = new HashSet<>();   // 空间的属性


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
        name = "space_device",
        joinColumns = @JoinColumn(name = "spaceId"),
        inverseJoinColumns = @JoinColumn(name = "deviceId")
    )
    private Set<DeviceInfo> spaceDevices = new HashSet<>();

}