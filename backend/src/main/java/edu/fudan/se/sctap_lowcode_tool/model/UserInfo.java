package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;   // 用户的唯一标识符

    @Column(nullable = false)
    private String userName;   // 用户的用户名

    @ManyToMany
    @JoinTable(name = "user_project",
            joinColumns = @JoinColumn(name = "userId"),
            inverseJoinColumns = @JoinColumn(name = "projectId"))
    private Set<ProjectInfo> projects = new HashSet<>();  // 用户有权限的场景

}
