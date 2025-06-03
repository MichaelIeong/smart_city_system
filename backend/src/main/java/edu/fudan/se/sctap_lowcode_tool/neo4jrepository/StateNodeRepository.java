package edu.fudan.se.sctap_lowcode_tool.neo4jrepository;

import edu.fudan.se.sctap_lowcode_tool.neo4jmodel.StateNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateNodeRepository extends Neo4jRepository<StateNode, Integer> {
}
