package edu.fudan.se.sctap_lowcode_tool.neo4jRepository;

import edu.fudan.se.sctap_lowcode_tool.neo4jModel.DeviceNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceNodeRepository extends Neo4jRepository<DeviceNode, Long> {
    Optional<DeviceNode> findByDeviceId(String deviceId);
}