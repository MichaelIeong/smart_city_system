package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_history")
@Data
public class EventHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "object_id")
    private Integer objectId;

    @Lob
    @Column(name = "event_data")
    private String eventData;

    @Column(name = "data_value")
    private String dataValue;

    @Column(name = "state")
    private String state;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "event_details")
    private String eventDetails;
}