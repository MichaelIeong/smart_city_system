package edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * SpaceEventHistory 环境事件历史记录
 * <p>
 * 当系统收到新发布的环境事件时，会创建一条历史记录，包含事件ID、负载数据和创建时间。
 * 该历史记录用于后续的count函数计算，以便统计特定时间窗口内的事件数量。
 * @author Lin Yicheng
 * @since 2026-01-12
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class SpaceEventHistory {

    /**
     * <b>主键ID</b><br/>
     * 每条环境事件历史记录的唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * <b>环境事件ID</b><br/>
     * 表明所属的环境事件，例如 "truck_spill", "truck_spill_cross_region"
     */
    @NotNull
    @Column(nullable = false)
    private String spaceEventId;

    /**
     * <b>负载数据</b><br/>
     * 存储该次环境事件的负载信息，采用 JSON 格式存储。<br/>
     * 例如：{"targetPlateNo": "云A123456", "location": "gridA"}
     */
    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> payload;

    /**
     * <b>创建时间</b><br/>
     * 记录该历史事件的创建时间，用于后续的时间窗口计算。<br/>
     * 该字段由 JPA 自动维护，为该记录创建时的时间戳，而非事件的发生时间。
     */
    @NotNull
    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
