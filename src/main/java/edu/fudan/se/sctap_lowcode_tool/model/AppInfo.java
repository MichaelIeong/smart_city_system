package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "apps")
@Data
public class AppInfo {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private long appId; // 主键字段，假设每个AppInfo都有一个唯一的ID

//    @Column(name = "event_type", nullable = false)
//    private String eventType; // 触发 trigger
//
//    @Column(name = "action", nullable = false)
//    private String action; // 行动
//
//    @Column(name = "scenario_description")
//    private String scenarioDescription; // 场景描述
//
//    @Column(name = "scenario_computing")
//    private String scenarioComputing; // 场景计算
//
//    @Column(name = "user")
//    private String user; // 场景计算

    @Column(length = 65536)
    private String appJson; // 将application以json的格式存到数据库
}