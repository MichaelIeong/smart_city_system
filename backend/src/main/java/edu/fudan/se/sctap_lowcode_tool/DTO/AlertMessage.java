package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

@Data
public class AlertMessage {
    private String location;
    private String eventType;
    private String time;
    private String command;
}
