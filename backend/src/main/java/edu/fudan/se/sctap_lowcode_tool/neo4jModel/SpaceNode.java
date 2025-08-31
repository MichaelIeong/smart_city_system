package edu.fudan.se.sctap_lowcode_tool.neo4jModel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("Space")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SpaceNode {

    /** 作为主键，Project 内唯一（保留你现有的主键形态） */
    @Id
    @EqualsAndHashCode.Include
    private Integer spaceId;

    private Integer projectId;   // 项目的唯一标识符
    private String spaceName;    // 空间名称
    private String fixedProperties; // 固定属性（JSON字符串）
    private String description;  // 描述（如“卧室1”）


    /** 与其它空间的相邻关系（如果你的项目已有该建模，保留；没有也可删除） */
    @Relationship(type = "ADJACENT_TO", direction = Relationship.Direction.OUTGOING)
    private Set<SpaceNode> adjacentSpaces = new HashSet<>();

    /**
     * 反向端：DeviceType —AVAILABLE_IN→ Space
     * 方案一要求 DeviceType 全局复用，一个类型可以连接多个 Space。
     */
    @Relationship(type = "AVAILABLE_IN", direction = Relationship.Direction.INCOMING)
    private Set<DeviceTypeNode> deviceTypes = new HashSet<>();

    /**
     * Space —HAS_ACTUATING_FUNCTION→ ActuatingFunction
     * 注意：这是空间层面的“可用/归属”关系，不包含 url。
     * url 依然保留在 “Device —HAS_ACTUATING_FUNCTION→ ActuatingFunction” 的带属性关系里
     * （ActuatingFunctionDeviceRelation），不做迁出。
     */
    @Relationship(type = "HAS_ACTUATING_FUNCTION", direction = Relationship.Direction.OUTGOING)
    private Set<ActuatingFunctionSpaceRelation> actuatingFunctions = new HashSet<>();

    // ====== 便捷方法：相邻空间维护（可选） ======
    public void addAdjacentSpace(SpaceNode space) {
        if (space == null || this == space) return;
        if (this.spaceId != null && this.spaceId.equals(space.spaceId)) return;
        if (this.adjacentSpaces.contains(space)) return;

        this.adjacentSpaces.add(space);
        if (!space.getAdjacentSpaces().contains(this)) {
            space.getAdjacentSpaces().add(this);
        }
    }

    public void removeAdjacentSpace(SpaceNode space) {
        if (space == null) return;
        if (!this.adjacentSpaces.contains(space)) return;

        this.adjacentSpaces.remove(space);
        if (space.getAdjacentSpaces().contains(this)) {
            space.getAdjacentSpaces().remove(this);
        }
    }
}