package edu.fudan.se.sctap_lowcode_tool.neo4jRepository;


import edu.fudan.se.sctap_lowcode_tool.neo4jModel.ServiceNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceNodeRepository extends Neo4jRepository<ServiceNode, Integer> {
    // 新增
    // 更新
    // 删除
    // 查找（根据serviceid，根据spaceid）
    // 根据 serviceId 查找服务（默认通过 ID 实现，也可以显式写）
    Optional<ServiceNode> findByServiceId(Integer serviceId);

    // 根据空间 spaceId 查找该空间下的所有服务
    @Query("""
        MATCH (s:Service)-[:INSTALLED_IN]->(space:Space {spaceId: $spaceId})
        RETURN s
        """)
    List<ServiceNode> findAllBySpaceId(Integer spaceId);

    // 删除指定 serviceId 的服务节点
    void deleteByServiceId(Integer serviceId);

    @Query("MATCH (s:Service) RETURN coalesce(max(s.serviceId), 0)")
    Integer findMaxServiceId();
}
