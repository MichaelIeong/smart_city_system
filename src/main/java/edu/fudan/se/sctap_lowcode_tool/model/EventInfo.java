package edu.fudan.se.sctap_lowcode_tool.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "event_info")
@Data
public class EventInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "event_id", nullable = false)
    private int appId; // 主键字段，假设每个AppInfo都有一个唯一的ID

}