package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "space_info")
@Data
public class SpaceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "space_id", nullable = false)
    private int spaceId;   // 空间的唯一标识符

    @Column(name = "name", nullable = false)
    private String spaceName;   // 空间的名称

    @Column(name = "type")
    private String spaceType;   // 空间的类型，例如“卧室”、“客厅”

    @Column(name = "description")
    private String spaceDescription;   // 空间的描述，例如“卧室1”

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private Set<DeviceInfo> devices;

}