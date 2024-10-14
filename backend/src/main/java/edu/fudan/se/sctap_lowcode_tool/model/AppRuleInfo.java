package edu.fudan.se.sctap_lowcode_tool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table
@Data
public class AppRuleInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id; // 全局唯一标识

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectInfo project;   // 应用规则所属的Project

    private String description; // 应用规则的描述

    @Column(columnDefinition = "LONGTEXT")
    private String ruleJson; // 应用规则的JSON格式

    private LocalDateTime updateTime; // 最近一次更新时间

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppRuleInfo that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(description, that.description) && Objects.equals(ruleJson, that.ruleJson) && Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, ruleJson, updateTime);
    }

    @Override
    public String toString() {
        return "AppRuleInfo{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", ruleJson='" + ruleJson + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }
}
