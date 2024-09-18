package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_Info")
@Data
public class AppInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "app_name")
    private String appName; // app的名字

    @Column(name = "app_json",length = 65536)
    private String appJson; // 将application以json的格式存到数据库

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppJson() {
        return appJson;
    }

    public void setAppJson(String appJson) {
        this.appJson = appJson;
    }
}