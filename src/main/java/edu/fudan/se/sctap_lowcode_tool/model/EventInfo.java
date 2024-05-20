package edu.fudan.se.sctap_lowcode_tool.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "events")
@Data
public class EventInfo {
    @Id
    @GeneratedValue
    @Column
    private long eventId; // 主键字段，假设每个AppInfo都有一个唯一的ID

}