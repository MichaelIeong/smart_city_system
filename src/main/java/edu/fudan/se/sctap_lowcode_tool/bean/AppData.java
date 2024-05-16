package edu.fudan.se.sctap_lowcode_tool.bean;

/**
 * @author ：sunlinyue
 * @date ：Created in 2024/5/15 21:31
 * @description：
 * @modified By：
 * @version: $
 */
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public DSL getDsl() {
        return dsl;
    }

    public void setDsl(DSL dsl) {
        this.dsl = dsl;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    @Override
    public String toString() {
        return "AppData{" +
                "user='" + user + '\'' +
                ", dsl=" + dsl +
                ", startTime=" + startTime +
                ", endTime='" + endTime + '\'' +
                ", app='" + app + '\'' +
                '}';
    }
}