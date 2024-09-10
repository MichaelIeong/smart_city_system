package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;   // 用户的唯一标识符

    private String userName;   // 用户的用户名

    @ManyToMany
    @JoinTable(name = "user_scenario",
            joinColumns = @JoinColumn(name = "userId"),
            inverseJoinColumns = @JoinColumn(name = "scenarioId"))
    private Set<ScenarioInfo> scenarios = new HashSet<>();  // 用户有权限的场景

}
