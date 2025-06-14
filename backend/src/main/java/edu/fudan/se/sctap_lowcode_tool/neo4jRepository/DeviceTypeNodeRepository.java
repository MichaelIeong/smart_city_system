package edu.fudan.se.sctap_lowcode_tool.neo4jRepository;

import edu.fudan.se.sctap_lowcode_tool.neo4jModel.DeviceTypeNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceTypeNodeRepository extends Neo4jRepository<DeviceTypeNode, Long> {
}
