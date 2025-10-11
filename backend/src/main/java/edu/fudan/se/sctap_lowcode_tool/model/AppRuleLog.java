package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_rule_log")
@Data
public class AppRuleLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "wait_value", nullable = false)
    private String waitValue;

    @Lob
    @Column(name = "logs", nullable = false, columnDefinition = "LONGTEXT")
    private String logs;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
