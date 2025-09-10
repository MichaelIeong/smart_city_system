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
    @JsonIgnore
    private FusionRule rule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    @JsonIgnore
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
}