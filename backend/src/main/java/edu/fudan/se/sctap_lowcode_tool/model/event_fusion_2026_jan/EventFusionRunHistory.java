package edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.DataEvent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * EventFusionRunHistory 事件融合运行历史记录
 * <p>
 * 该实体用于记录每次事件融合规则的运行情况，包括触发的事件、各步骤的输出结果、最终发布的事件以及运行日志。
 * @author Lin Yicheng
 * @since 2026-01-20
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EventFusionRunHistory {

    /**
     * <b>主键ID</b><br/>
     * 每条事件融合运行历史记录的唯一标识
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * <b>规则名称</b><br/>
     * 本次运行所对应的事件融合规则名称
     */
    @Nullable
    private String ruleName;

    /**
     * <b>触发事件列表</b>
     */
    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<DataEvent> triggers;

    /**
     * <b>步骤输出结果</b><br/>
     * 存储各个步骤的输出结果，键为步骤名称，值为对应的输出数据
     */
    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Map<String, Object>> stepOutputs;

    /**
     * <b>发布事件</b><br/>
     * 本次运行最终发布的事件数据
     */
    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private DataEvent publishedEvent;

    /**
     * <b>运行结果</b>
     */
    @Nullable
    private Boolean isSuccess;

    /**
     * <b>运行日志</b>
     */
    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> logs;

    /**
     * <b>创建时间</b><br/>
     * 该字段由 JPA 自动维护，为该记录创建时的时间戳。
     */
    @NotNull
    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

}
