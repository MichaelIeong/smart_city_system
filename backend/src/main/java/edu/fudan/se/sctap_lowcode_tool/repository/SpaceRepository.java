package edu.fudan.se.sctap_lowcode_tool.repository;


import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpaceRepository extends JpaRepository<SpaceInfo, Integer> {
    Optional<SpaceInfo> findBySpaceName(String name);

    // 根据 projectId 查找所有 Space
    List<SpaceInfo> findByProjectId(int projectId);
}
