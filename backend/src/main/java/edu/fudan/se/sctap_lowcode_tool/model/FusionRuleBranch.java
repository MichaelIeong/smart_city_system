package edu.fudan.se.sctap_lowcode_tool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "fusion_rule_branch")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FusionRuleBranch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private int branchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    @JsonIgnore // ✅ 避免把主干规则懒代理序列化出去
    private FusionRule rule;

    // ✅ 正确映射 space（可为空）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    @JsonIgnore // ✅ 不直接序列化 LAZY 对象，前端用 spaceId 即可
    private SpaceInfo space;

    @Column(name = "branch_index", nullable = false)
    private Integer branchIndex;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "fusion_target")
    private String fusionTarget;

    @Column(name = "status", nullable = false)
    private String status = "inactive";

    @Lob
    @Column(name = "rule_json", columnDefinition = "LONGTEXT")
    private String ruleJson;

    @Lob
    @Column(name = "flow_json", columnDefinition = "LONGTEXT")
    private String flowJson;

    // ✅ 供前端使用的只读字段：返回 spaceId
    @Transient
    public Integer getSpaceId() {
        return space == null ? null : space.getSpaceId();
    }
}