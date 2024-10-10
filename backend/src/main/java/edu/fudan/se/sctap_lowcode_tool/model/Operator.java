package edu.fudan.se.sctap_lowcode_tool.model;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "operator")
@Data
public class Operator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operator_id", nullable = false)
    private int operatorId; // 主键字段

    @Column(name = "operator_name")
    private String operatorName; // 算子的名字

    @Column(name = "operator_api")
    private String operatorApi; // 算子的api(复杂算子，简单算子为null)

    @Column(name = "output_name")
    private String outputName; // 输出的名字
}
