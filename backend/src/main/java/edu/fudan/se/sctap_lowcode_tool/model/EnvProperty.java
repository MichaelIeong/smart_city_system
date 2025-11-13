package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "env_property")
public class EnvProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grid_id", nullable = false)
    private String gridId;

    @Column(name = "property_name", nullable = false)
    private String propertyName;

    @Column(name = "description")
    private String description;
}