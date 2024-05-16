package edu.fudan.se.sctap_lowcode_tool.model;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.core.util.Json;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "AppInfo")
@Data
public class AppInfo {
    @Id
    @Column(name = "app_id", nullable = false)
    private int appId; // 主键字段，假设每个AppInfo都有一个唯一的ID

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

    @Column(name = "app_json")
    private String appJson; // 将application以json的格式存到数据库
}