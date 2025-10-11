package edu.fudan.se.sctap_lowcode_tool.neo4jRepository;

import edu.fudan.se.sctap_lowcode_tool.neo4jModel.EventTypeNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventTypeNodeRepository extends Neo4jRepository<EventTypeNode, Integer> {

    @Query("MATCH (e:EventType) RETURN e")
    List<EventTypeNode> findAllEventTypes();

    @Query("MATCH (e:EventType)-[:LOCATED_IN]->(s:Space {spaceId: $spaceId}) RETURN e")
    List<EventTypeNode> findBySpaceId(Integer spaceId);

    void deleteByEventTypeId(String eventTypeId);
}