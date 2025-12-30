package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.GridMesh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GridMeshRepository extends JpaRepository<GridMesh, String> {
    // 根据 meshNature 和 meshType 查询网格列表
    List<GridMesh> findByMeshNatureAndMeshType(String meshNature, String meshType);
}
