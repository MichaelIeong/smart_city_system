package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "properties")
@Data
public class PropertyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer propertyId;   // 属性的唯一标识符

    private String propertyName;   // 属性的名称

    private String propertyValue;   // 属性的值

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spaceId")
    private SpaceInfo space;   // 属性所属的空间

}
