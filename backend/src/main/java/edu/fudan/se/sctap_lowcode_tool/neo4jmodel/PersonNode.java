package edu.fudan.se.sctap_lowcode_tool.neo4jmodel;

import org.springframework.data.neo4j.core.schema.*;
import lombok.Data;

import java.util.Objects;

@Node("Person")
@Data
public class PersonNode {

    @Id
    @GeneratedValue
    private Long id;   // Neo4j唯一主键

    private String personName;   // 人员姓名

    @Relationship(type = "LOCATED_IN", direction = Relationship.Direction.OUTGOING)
    private SpaceNode currentSpace;   // 当前所在空间，允许为 null

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonNode that)) return false;
        return Objects.equals(id, that.id) &&
                Objects.equals(personName, that.personName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, personName);
    }
}