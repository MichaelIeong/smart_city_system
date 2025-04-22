package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;

@Entity
@Table(name = "person")
@Data
public class PersonInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;   // 人员的唯一标识符

    @Column(name = "person_id", nullable = false, unique = true)
    private String personId;   // 人员的唯一标识符

    @Column(nullable = false)
    private String personName;   // 人员的姓名

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private SpaceInfo currentSpace;   // 与空间的关联，表示人员所在的空间，允许为null

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonInfo that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(personId, that.personId) && Objects.equals(personName, that.personName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, personId, personName);
    }
}