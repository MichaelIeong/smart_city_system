package edu.fudan.se.sctap_lowcode_tool.model;


import jakarta.persistence.*;

@Entity
public class PropertySpace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private PropertyInfo property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private SpaceInfo space;

    private String propertyValue;

    @Override
    public String toString() {
        return "PropertySpace{" +
                "property=" + property.getPropertyKey() +
                ", space=" + space.getSpaceName() +
                ", propertyValue='" + propertyValue + '\'' +
                '}';
    }
}
