package edu.fudan.se.sctap_lowcode_tool.utils.milvus.entity;

import lombok.Data;

@Data
public class AppRuleRecord {
    private String id; // 全局唯一标识
    private String description; // 应用规则的描述

    private Float score;

    public AppRuleRecord(String id, String description, Float score) {
        this.id = id;
        this.description = description;
        this.score = score;
    }

    public AppRuleRecord(String id, String description) {
        this.id = id;
        this.description = description;
    }
}
