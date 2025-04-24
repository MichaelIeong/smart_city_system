package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ActuatingFunctionDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actuating_function_id", nullable = false)
    private ActuatingFunctionInfo actuatingFunction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private DeviceInfo device;

    private String url;  // 设备的URL

    private String description;  // 设备的URL

}
