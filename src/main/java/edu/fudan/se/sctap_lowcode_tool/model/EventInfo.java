package edu.fudan.se.sctap_lowcode_tool.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "EventInfo")
@Data
public class EventInfo {
    @Id
    @Column(name = "event_id", nullable = false)
    private int appId; // 主键字段，假设每个AppInfo都有一个唯一的ID

}