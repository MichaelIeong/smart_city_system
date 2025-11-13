package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "env_service")
public class EnvService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grid_id", nullable = false)
    private String gridId;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "description")
    private String description;

    // TEXT 类型，用于存 JSON 字符串
    @Column(name = "service_json", columnDefinition = "TEXT", nullable = false)
    private String serviceJson;
}