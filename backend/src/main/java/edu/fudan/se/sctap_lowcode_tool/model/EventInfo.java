package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "events",
        uniqueConstraints = {@UniqueConstraint(
                columnNames = {"space_id", "event_id"}
        )}
)
@Data
public class EventInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id; // 全局唯一标识

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private SpaceInfo parentingSpace;

    @Column(name = "event_id", nullable = false)
    private String eventId; // 事件ID(由用户自定义, Project内唯一)

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private PropertyInfo propertyToUpdate;

    @Column(nullable = false)
    private String eventType; // 事件类型, 例如“温度过高”

}