package edu.fudan.se.sctap_lowcode_tool.neo4jModel;

import org.springframework.data.neo4j.core.schema.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Node("Space")
@Data
public class SpaceNode {

    @Id
    private Integer spaceId;      // 作为主键，Project 内唯一

    private String name;   // 空间名称（与 spaceName 相同）
    private String spaceName;    // 空间名称
    private String fixedProperties; // 固定属性（JSON字符串）
    private String description;  // 描述（如“卧室1”）

    @Relationship(type = "LOCATED_IN", direction = Relationship.Direction.INCOMING)
    private Set<DeviceNode> spaceDevices = new HashSet<>();

    @Relationship(type = "ADJACENT_TO", direction = Relationship.Direction.OUTGOING)
    private Set<SpaceNode> adjacentSpaces = new HashSet<>();

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
        this.name = spaceName; // 自动同步
    }

    public void addAdjacentSpace(SpaceNode space) {
        if (this.spaceId.equals(space.spaceId) || this.adjacentSpaces.contains(space)) {
            return;
        }
        adjacentSpaces.add(space);
        space.getAdjacentSpaces().add(this);
    }

    public void removeAdjacentSpace(SpaceNode space) {
        if (this.spaceId.equals(space.spaceId) || !this.adjacentSpaces.contains(space)) {
            return;
        }
        adjacentSpaces.remove(space);
        space.getAdjacentSpaces().remove(this);
    }
}