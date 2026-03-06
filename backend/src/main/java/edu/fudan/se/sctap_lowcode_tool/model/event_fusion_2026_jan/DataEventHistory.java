package edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule;
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
 * DataEventHistory 数据事件历史记录
 * <p>
 * 当系统采集到新的 DataEvent（含传感器事件与环境事件）时，会创建一条历史记录，
 * 包含事件ID、事件来源类型、负载数据和创建时间。
 * 该历史记录用于后续的 count 函数计算，以便统计特定时间窗口内的事件数量。
 * @author Lin Yicheng
 * @since 2026-01-12
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class DataEventHistory {

    /**
     * <b>主键ID</b><br/>
     * 每条数据事件历史记录的唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * <b>事件ID</b><br/>
     * 表明所属的数据事件类型，例如 "truck_spill", "truck_spill_cross_region"
     */
    @NotNull
    @Column(nullable = false)
    private String eventId;

    /**
     * <b>事件来源类型</b><br/>
     * 标识该事件是传感器事件（sensorEvent）还是环境事件（spaceEvent）。
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventFusionRule.EventSource eventSource;

    /**
     * <b>负载数据</b><br/>
     * 存储该次数据事件的负载信息，采用 JSON 格式存储。<br/>
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
