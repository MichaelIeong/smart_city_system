package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_history")
@Data
// TODO: 表结构需要修改，等待重构
public class EventHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int historyId;   // 历史记录的唯一标识符，应有其自己的ID

    @Column(nullable = false)
    private String eventType; // 事件类型

    @Column(nullable = false)
    private String location; // 事件发生的位置

    @Column(nullable = false)
    private int objectId; // 事件相关对象的ID

    @Column
    private String eventData; // 事件数据

    @Column
    private String dataValue; // 数据值

    @Column
    private String state; // 状态

    @Column(nullable = false)
    private LocalDateTime timestamp; // 事件发生的时间

    @Column
    private String eventDetails; // 事件详情
}