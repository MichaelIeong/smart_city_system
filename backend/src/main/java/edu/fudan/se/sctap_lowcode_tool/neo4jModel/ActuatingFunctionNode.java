package edu.fudan.se.sctap_lowcode_tool.neo4jModel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("ActuatingFunction")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ActuatingFunctionNode {

    @Id
    @EqualsAndHashCode.Include
    private Integer actuatingFunctionId;

    private String functionName;

    private String description;
}