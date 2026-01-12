package edu.fudan.se.sctap_lowcode_tool.model;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * EventFusionRuleEntity 事件融合规则实体类
 * <p>
 * 包括 主键ID 和 事件融合规则内容(JSON形式存储)。
 * @author Lin Yicheng
 * @since 2026-01-09
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class EventFusionRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 事件融合规则内容，存储为 JSON 格式
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json", nullable = false)
    @NotNull private EventFusionRule eventFusionRule;
}

