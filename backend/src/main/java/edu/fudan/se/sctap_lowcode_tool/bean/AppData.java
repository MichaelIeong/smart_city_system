package edu.fudan.se.sctap_lowcode_tool.bean;

import lombok.Data;

@Data
public class AppData {
    private String user;
    private DSL dsl;
    private long startTime;
    private String endTime;
    private String app;

    public AppData() {
    }

    public AppData(String user, DSL dsl, long startTime, String endTime, String app) {
        this.user = user;
        this.dsl = dsl;
        this.startTime = startTime;
        this.endTime = endTime;
        this.app = app;
    }
}