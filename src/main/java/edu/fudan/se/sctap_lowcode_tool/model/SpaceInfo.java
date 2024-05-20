package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "spaces")
@Data
public class SpaceInfo {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private long spaceId;   // 空间的唯一标识符

    @Column(nullable = false)
    private String spaceName;   // 空间的名称

    @Column
    private String type;   // 空间的类型，例如“卧室”、“客厅”

    @Column
    private String description;   // 空间的描述，例如“卧室1”

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "spaceId")
    private Set<DeviceInfo> spaceDevices;

}