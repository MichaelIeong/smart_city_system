package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_info")
@Data
public class MessageInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String uuid;

    private int messageType;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private LocalDateTime createTime;
}
