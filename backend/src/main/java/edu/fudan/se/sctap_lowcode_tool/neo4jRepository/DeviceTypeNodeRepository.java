package edu.fudan.se.sctap_lowcode_tool.neo4jRepository;

import edu.fudan.se.sctap_lowcode_tool.neo4jModel.DeviceTypeNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTypeNodeRepository extends Neo4jRepository<DeviceTypeNode, Integer> {

    // 根據 deviceTypeId 查找（需保證 DeviceTypeNode 中 deviceTypeId 是唯一的）
    Optional<DeviceTypeNode> findByDeviceTypeId(Integer deviceTypeId);

    @Query("""
    MATCH (s:Space {spaceId: $spaceId})<-[:AVAILABLE_IN]-(dt:DeviceType)
    OPTIONAL MATCH (dt)-[:HAS_FUNCTION]->(af:ActuatingFunction)
    RETURN dt, collect(af) AS af
    ORDER BY dt.deviceTypeName
    """)
    List<DeviceTypeNode> findDeviceTypesWithFunctionsBySpaceId(Integer spaceId);

}