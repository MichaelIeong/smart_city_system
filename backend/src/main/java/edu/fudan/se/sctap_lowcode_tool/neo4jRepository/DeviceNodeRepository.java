package edu.fudan.se.sctap_lowcode_tool.neo4jRepository;

import edu.fudan.se.sctap_lowcode_tool.neo4jModel.DeviceNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceNodeRepository extends Neo4jRepository<DeviceNode, String> {

    @Query("""
        MATCH (d:Device {deviceId: $deviceId})
        OPTIONAL MATCH (d)-[r1:INSTALLED_IN]->(s:Space)
        OPTIONAL MATCH (d)-[r2:BELONGS_TO]->(t:DeviceType)
        OPTIONAL MATCH (d)<-[r3:HAS_STATE]-(sd:StateDeviceRelation)-[:STATE_OF]->(state:State)
        OPTIONAL MATCH (d)-[r4:HAS_FUNCTION]->(af:ActuatingFunctionDeviceRelation)-[:IMPLEMENT]->(f:ActuatingFunction)
        RETURN d, collect(r1), collect(s), 
                  collect(r2), collect(t), 
                  collect(r3), collect(sd), collect(state), 
                  collect(r4), collect(af), collect(f)
    """)
    Optional<DeviceNode> findDeviceWithAllRelationsByDeviceId(String deviceId);

    void deleteByDeviceId(String deviceId);
}