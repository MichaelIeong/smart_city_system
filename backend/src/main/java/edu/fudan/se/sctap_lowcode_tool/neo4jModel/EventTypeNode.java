package edu.fudan.se.sctap_lowcode_tool.neo4jModel;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Objects;

@Node("EventType")
@Data
public class EventTypeNode {

    @Id
    private Integer eventTypeId;  // 事件类型唯一标识（如 manhole-flooding）

    private String eventType;    // 事件类型名称（如 井盖水浸）

    @Relationship(type = "LOCATED_IN", direction = Relationship.Direction.OUTGOING)
    private SpaceNode currentSpace;   // 当前所在空间，允许为 null

    private String location;     // 事件典型发生位置（如 “井盖_城市道路交叉口”）

    private String description;  // 类型说明

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventTypeNode that)) return false;
        return Objects.equals(eventTypeId, that.eventTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventTypeId);
    }
}