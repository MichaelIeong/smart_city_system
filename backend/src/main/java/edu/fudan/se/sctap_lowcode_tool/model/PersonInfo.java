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
    @Column(name = "person_id")  // 数据库字段命名保持一致
    private Integer personId;     // 将原 id 改为 personId

    @Column(nullable = false)
    private String personName;   // 人员姓名

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private SpaceInfo currentSpace;   // 所属空间，可为空

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonInfo that)) return false;
        return Objects.equals(personId, that.personId) &&
               Objects.equals(personName, that.personName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, personName);
    }
}