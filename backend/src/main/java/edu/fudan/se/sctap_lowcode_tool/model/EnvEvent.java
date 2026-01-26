package edu.fudan.se.sctap_lowcode_tool.model;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "env_event")
public class EnvEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "description", nullable = false)
    private String description;

    // TEXT 类型，存 JSON 字符串
    @Column(name = "event_json", columnDefinition = "TEXT", nullable = false)
    private String eventJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    @Nullable private EventFusionRule ruleDsl;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "cross_region", nullable = false)
    private Boolean crossRegion = true; // 是否跨网格

    @Column(name = "create_time")
    private LocalDateTime createTime; // 创建时间

    @Column(name = "depend_dtypes")
    private String dependDtypes; // 依赖的设备类型
}
