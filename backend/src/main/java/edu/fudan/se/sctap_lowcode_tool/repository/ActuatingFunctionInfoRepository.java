package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.ActuatingFunctionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActuatingFunctionInfoRepository extends JpaRepository<ActuatingFunctionInfo, Integer> {
    // 根据 name 查询 id
    @Query("SELECT a.id FROM ActuatingFunctionInfo a WHERE a.name = :name")
    Integer findIdByName(@Param("name") String name);
}
