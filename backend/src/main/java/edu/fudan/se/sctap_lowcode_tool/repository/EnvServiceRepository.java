package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvServiceRepository extends JpaRepository<EnvService, Integer> {
    @Query("""
        SELECT DISTINCT a FROM EnvService a
        JOIN EnvServiceGrid g ON g.envServiceId = a.id
        WHERE g.gridId = :gridId
        AND g.enabled = TRUE
    """)
    List<EnvService> findByGridId(@Param("gridId") String gridId);
}
