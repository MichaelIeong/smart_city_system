package edu.fudan.se.sctap_lowcode_tool.model;

import lombok.Data;

@Data
public class AppInfo {
    //触发 trigger
    String eventType;
    String action;
    //场景描述 Scenario Description
    //场景计算 Scenario Computing
}
