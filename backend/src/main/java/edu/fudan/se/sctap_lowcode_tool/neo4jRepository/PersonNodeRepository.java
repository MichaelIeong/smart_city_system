package edu.fudan.se.sctap_lowcode_tool.neo4jRepository;

import edu.fudan.se.sctap_lowcode_tool.neo4jModel.PersonNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.SpaceNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonNodeRepository extends Neo4jRepository<PersonNode, Long> {
    List<PersonNode> findByCurrentSpace(SpaceNode spaceNode);
}
