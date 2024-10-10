package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ActuatingFunctionDeviceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "actuating_function_id", nullable = false)
    private ActuatingFunctionInfo actuatingFunction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_type_id", nullable = false)
    private DeviceTypeInfo deviceType;

}
