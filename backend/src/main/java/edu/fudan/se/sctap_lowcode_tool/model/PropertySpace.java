package edu.fudan.se.sctap_lowcode_tool.model;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;

@Entity
@Data
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertySpace that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(propertyValue, that.propertyValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, propertyValue);
    }

    @Override
    public String toString() {
        return "PropertySpace{" +
                "property='" + property.getPropertyKey() + "'" +
                ", space='" + space.getSpaceName() + "'" +
                ", propertyValue='" + propertyValue + '\'' +
                '}';
    }
}
