package edu.fudan.se.sctap_lowcode_tool.neo4jRepository;

import edu.fudan.se.sctap_lowcode_tool.neo4jModel.DeviceNode;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface DeviceNodeRepository extends Neo4jRepository<DeviceNode, Integer> {
    @Query("""
    MATCH (d:Device {deviceId: $deviceId})
    OPTIONAL MATCH (d)-[r1:INSTALLED_IN]->(s:Space)
    OPTIONAL MATCH (d)-[r2:BELONGS_TO]->(t:DeviceType)
    OPTIONAL MATCH (s1:State)-[r3:HAS_STATE]->(d)
    OPTIONAL MATCH (d)-[r4:HAS_FUNCTION]->(f:ActuatingFunction)
    RETURN d, collect(r1), collect(s), 
              collect(r2), collect(t), 
              collect(r3), collect(s1), 
              collect(r4), collect(f)
""")
    Optional<DeviceNode> findDeviceWithAllRelationsByDeviceId(Integer deviceId);

    @Modifying
    @Transactional
    @Query("MATCH (d:Device {deviceId: $deviceId}) DETACH DELETE d")
    void deleteByDeviceId(Integer deviceId);
}
