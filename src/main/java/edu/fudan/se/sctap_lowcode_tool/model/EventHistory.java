package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "event_history")
@Data
//todo: 将DeviceHistory改为EventHistory，新增应用执行的历史记录
public class EventHistory {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private long historyId;   // 历史记录的唯一标识符，应有其自己的ID

}