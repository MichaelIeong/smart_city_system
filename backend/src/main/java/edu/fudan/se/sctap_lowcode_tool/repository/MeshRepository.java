package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.MeshInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeshRepository extends JpaRepository<MeshInfo, Integer> {

    List<MeshInfo> findByProjectId(Integer projectId);

    void deleteByProjectId(Integer projectId);
}