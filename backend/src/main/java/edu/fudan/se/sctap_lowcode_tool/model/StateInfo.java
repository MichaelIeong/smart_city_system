package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "states")
@Data
public class StateInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stateId;   // 状态的唯一标识符

    @Column(nullable = false)
    private String stateKey;   // 状态的名称

}
