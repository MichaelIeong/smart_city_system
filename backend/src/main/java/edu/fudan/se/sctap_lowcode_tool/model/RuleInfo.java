package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "rule_Info")
@Data
public class RuleInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "rule_id", nullable = false)
    private int ruleId; // 主键字段，假设每个RuleInfo都有一个唯一的ID


    @Column(name = "project_name")
    private String projectName; // 项目的名字

    @Column(name = "rule_name")
    private String ruleName; // 规则的名字

    @Column(name = "rule_json",length = 65536)
    private String ruleJson; // 将rule以json的格式存到数据库

    @Column(name = "flow_json",length = 65536)
    private String flowJson; // 将flow信息以json的格式存到数据库

    @Column(name = "rule_status")
    private int ruleStatus; // 规则状态

    @Column(name = "call_count")
    private int callCount; // 调用次数

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleJson() {
        return ruleJson;
    }

    public void setRuleJson(String ruleJson) {
        this.ruleJson = ruleJson;
    }

    public String getFlowJson() {
        return flowJson;
    }

    public void setFlowJson(String flowJson) {
        this.flowJson = flowJson;
    }

    public int getRuleStatus() {
        return ruleStatus;
    }

    public void setRuleStatus(int ruleStatus) {
        this.ruleStatus = ruleStatus;
    }

    public int getCallCount() {
        return callCount;
    }

    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }
}