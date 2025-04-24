package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "device_configuration")
@Data
public class DeviceConfiguration {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "device_id")
        private int deviceId;

        @Column(name = "device_name")
        private String deviceName;

        @Lob
        @Column(name = "configuration", columnDefinition = "TEXT")
        private String configuration;

        @Lob
        @Column(name = "lha", columnDefinition = "TEXT")
        private String lha;
}