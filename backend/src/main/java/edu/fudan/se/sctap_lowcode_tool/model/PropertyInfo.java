package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "properties")
@Data
public class PropertyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer propertyId;   // 属性的唯一标识符

    private String propertyKey;   // 属性的名称

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<PropertySpace> parentingSpaces = new HashSet<>();   // 属性所属的空间

}
