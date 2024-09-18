package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

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
    private SpaceInfo spaceInfo;   // 事件所属的空间

    @Column(name = "event_id", nullable = false)
    private String eventId; // 事件ID(由用户自定义, Project内唯一)

    @Column(nullable = false)
    private String eventType; // 事件类型

    @Column(nullable = false)
    private String objectId; // 事件相关对象的ID

    @Column
    private String eventData; // 事件数据

    @Column
    private String dataValue; // 数据值

    @Column
    private String state; // 状态

    @Column
    private LocalDateTime timestamp; // 事件发生的时间

    @Column
    private String eventDetails; // 事件详情
}