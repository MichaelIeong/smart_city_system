package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "env_event")
public class EnvEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grid_id", nullable = false)
    private String gridId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "description", nullable = false)
    private String description;

    // TEXT 类型，存 JSON 字符串
    @Column(name = "event_json", columnDefinition = "TEXT", nullable = false)
    private String eventJson;
}
