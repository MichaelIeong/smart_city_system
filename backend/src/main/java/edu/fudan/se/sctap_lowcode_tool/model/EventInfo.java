package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
public class EventInfo {
    @Id
    @GeneratedValue
    @Column
    private String eventId; // 主键字段，假设每个EventInfo都有一个唯一的ID

    @Column(nullable = false)
    private String eventType; // 事件类型

    @Column(nullable = false)
    private String location; // 事件发生的位置

    @Column(nullable = false)
    private String objectId; // 事件相关对象的ID

    @Column
    private String eventData; // 事件数据

    @Column
    private String dataValue; // 数据值

    @Column
    private String state; // 状态

    //@Column(nullable = false)
    @Column
    private LocalDateTime timestamp; // 事件发生的时间

    @Column
    private String eventDetails; // 事件详情
}