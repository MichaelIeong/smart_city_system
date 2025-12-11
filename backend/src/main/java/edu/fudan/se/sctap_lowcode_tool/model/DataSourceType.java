package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "datasource_type")
public class DataSourceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "datasource_type", nullable = false)
    private String datasourceType;
}
