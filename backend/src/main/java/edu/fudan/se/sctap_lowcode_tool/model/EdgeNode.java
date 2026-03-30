package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "edge_node")
@Data
public class EdgeNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id; // 全局唯一标识

    @Column(name = "grid_id", nullable = false)
    private String gridId;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;
}
