package edu.fudan.se.sctap_lowcode_tool.neo4jModel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("DeviceType")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DeviceTypeNode {

    @Id
    @EqualsAndHashCode.Include
    private Integer deviceTypeId;

    private String deviceTypeName;

    private Boolean isSensor;


    @Relationship(type = "HAS_FUNCTION", direction = Relationship.Direction.OUTGOING)
    private Set<ActuatingFunctionNode> functions = new HashSet<>();

    /**
     * 新增：DeviceType —AVAILABLE_IN→ Space
     * 一个全局 DeviceType 可以复用，直接连到多个 Space。
     */
    @Relationship(type = "AVAILABLE_IN", direction = Relationship.Direction.OUTGOING)
    private Set<SpaceNode> spaces = new HashSet<>();

    // ====== 便捷方法 ======
    public void addSpace(SpaceNode space) {
        if (space == null) return;
        if (spaces == null) spaces = new HashSet<>();
        spaces.add(space);
    }

    public void removeSpace(SpaceNode space) {
        if (space == null) return;
        if (spaces != null) spaces.remove(space);
    }
}