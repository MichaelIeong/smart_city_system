package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "env_service")
public class EnvService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    // TEXT 类型，用于存 JSON 字符串
    @Column(name = "service_json", columnDefinition = "TEXT", nullable = false)
    private String serviceJson;

    // TEXT 类型，用于存 JSON 字符串
    @Column(name = "rule_json", columnDefinition = "TEXT", nullable = false)
    private String ruleJson;

    @Column(name = "cross_region", nullable = false)
    private Boolean crossRegion = true; // 是否跨网格

    @Column(name = "create_time")
    private LocalDateTime createTime; // 创建时间

}