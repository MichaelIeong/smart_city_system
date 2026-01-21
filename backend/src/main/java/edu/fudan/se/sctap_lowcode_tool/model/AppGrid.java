package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_grid")
@Data
public class AppGrid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id; // 全局唯一标识

    @Column(name = "grid_id", nullable = false)
    private String gridId;

    @Column(name = "app_rule_id", nullable = false)
    private Integer appRuleId;

    @Column(name = "enabled")
    private Boolean enabled = true; // 是否启用
}
