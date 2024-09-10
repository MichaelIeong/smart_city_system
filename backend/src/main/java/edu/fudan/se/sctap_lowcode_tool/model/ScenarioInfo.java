package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "scenarios")
@Data
public class ScenarioInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer scenarioId;   // 场景的唯一标识符

    private String scenarioName;   // 场景的名称

    @OneToMany(mappedBy = "scenario", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<SpaceInfo> spaces = new HashSet<>();   // 场景中包含的空间


}
