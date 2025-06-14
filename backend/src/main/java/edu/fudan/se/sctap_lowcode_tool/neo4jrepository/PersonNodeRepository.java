package edu.fudan.se.sctap_lowcode_tool.neo4jrepository;

import edu.fudan.se.sctap_lowcode_tool.neo4jmodel.PersonNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jmodel.SpaceNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonNodeRepository extends Neo4jRepository<PersonNode, Long> {
    List<PersonNode> findByCurrentSpace(SpaceNode spaceNode);
}
