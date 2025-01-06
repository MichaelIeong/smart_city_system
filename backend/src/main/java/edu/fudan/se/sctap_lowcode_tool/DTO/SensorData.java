package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

import java.util.List;

@Data
public class SensorData {

    private String sensorId;

    private String location;

    private String deviceName;

    private String deviceType;

    private List<String> function;
}
