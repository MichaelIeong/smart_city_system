package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "env_service_grid")
@Data
public class EnvServiceGrid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id; // 全局唯一标识

    @Column(name = "grid_id", nullable = false)
    private String gridId;

    @Column(name = "env_service_id", nullable = false)
    private Integer envServiceId;

    @Column(name = "enabled")
    private Boolean enabled = true; // 是否启用
}
