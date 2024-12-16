package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_history")
@Data
// TODO: 表结构需要修改，等待重构
public class EventHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;   // 历史记录的唯一标识符，应有其自己的ID

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private EventInfo eventInfo; // 发生的事件

    private int objectId; // 事件相关对象的ID

    @Column
    private String eventData; // 事件数据

    @Column(nullable = false)
    private LocalDateTime timestamp; // 事件发生的时间

}