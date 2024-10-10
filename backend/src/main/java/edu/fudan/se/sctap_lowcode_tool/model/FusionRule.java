package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "fusion_rule")
@Data
public class FusionRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id", nullable = false)
    private int ruleId; // 主键字段

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectInfo projectID; // 项目的ID

    @Column(name = "rule_name")
    private String ruleName; // 规则的名字

    @Column(name = "rule_json",length = 65536)
    private String ruleJson; // 将rule以json的格式存到数据库

    @Column(name = "flow_json",length = 65536,columnDefinition = "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String flowJson; // 将flow信息以json的格式存到数据库
}