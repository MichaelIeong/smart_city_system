package edu.fudan.se.sctap_lowcode_tool.neo4jmodel;

import org.springframework.data.neo4j.core.schema.*;
import lombok.Data;
import lombok.ToString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Node("Space")
@Data
public class SpaceNode {

    @Id
    @GeneratedValue
    private Long id;   // 空间唯一标识符（全局唯一）

    @Relationship(type = "IN_PROJECT", direction = Relationship.Direction.OUTGOING)
    @ToString.Exclude
    private ProjectNode projectInfo;

    private String spaceId;      // 用户设定的空间ID（Project内唯一）
    private String spaceName;    // 空间名称
    private String fixedProperties; // 固定属性（JSON字符串）
    private String description;  // 描述（如“卧室1”）

    @Relationship(type = "HAS_PROPERTY", direction = Relationship.Direction.INCOMING)
    private Set<PropertySpaceRelation> properties = new HashSet<>();

    @Relationship(type = "LOCATED_IN", direction = Relationship.Direction.INCOMING)
    private Set<DeviceNode> spaceDevices = new HashSet<>();

    @Relationship(type = "GENERATES_EVENT", direction = Relationship.Direction.INCOMING)
    private Set<EventNode> events = new HashSet<>();

    @Relationship(type = "PROVIDES_SERVICE", direction = Relationship.Direction.INCOMING)
    private Set<ServiceNode> services = new HashSet<>();

    @Relationship(type = "ADJACENT_TO", direction = Relationship.Direction.UNDIRECTED)
    private Set<SpaceNode> adjacentSpaces = new HashSet<>();

    public void addAdjacentSpace(SpaceNode space) {
        if (this.id.equals(space.id) || this.adjacentSpaces.contains(space)) {
            return;
        }
        adjacentSpaces.add(space);
        space.getAdjacentSpaces().add(this);
    }

    public void removeAdjacentSpace(SpaceNode space) {
        if (this.id.equals(space.id) || !this.adjacentSpaces.contains(space)) {
            return;
        }
        adjacentSpaces.remove(space);
        space.getAdjacentSpaces().remove(this);
    }
}