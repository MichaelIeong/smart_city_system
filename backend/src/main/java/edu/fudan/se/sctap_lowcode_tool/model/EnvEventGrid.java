package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "env_event_grid")
@Data
public class EnvEventGrid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id; // 全局唯一标识

    @Column(name = "grid_id", nullable = false)
    private String gridId;

    @Column(name = "env_event_id", nullable = false)
    private Integer envEventId;

    @Column()
    private Boolean enabled = true; // 是否启用
}
