package edu.fudan.se.sctap_lowcode_tool.neo4jRepository;

import edu.fudan.se.sctap_lowcode_tool.neo4jModel.DeviceTypeNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceTypeNodeRepository extends Neo4jRepository<DeviceTypeNode, Long> {

    // 根據 deviceTypeId 查找（需保證 DeviceTypeNode 中 deviceTypeId 是唯一的）
    Optional<DeviceTypeNode> findByDeviceTypeId(String deviceTypeId);
}