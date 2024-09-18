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

    @Column(nullable = false)
    private String passWord;   // 用户的密码

    @ManyToMany
    @JoinTable(name = "user_project",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id"))
    private Set<ProjectInfo> projects = new HashSet<>();  // 用户有权限的场景

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<ProjectInfo> getProjects() {
        return projects;
    }

    public void setProjects(Set<ProjectInfo> projects) {
        this.projects = projects;
    }
}
