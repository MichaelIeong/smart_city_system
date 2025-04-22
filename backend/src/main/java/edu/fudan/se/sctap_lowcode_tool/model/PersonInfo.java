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
    private Integer id;   // 資料庫唯一主鍵

    @Column(nullable = false)
    private String personName;   // 人員姓名

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private SpaceInfo currentSpace;   // 所屬空間，允許為 null

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonInfo that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(personName, that.personName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, personName);
    }
}