package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvEventRepository extends JpaRepository<EnvEvent, Integer> {
    @Query("""
        SELECT DISTINCT a FROM EnvEvent a
        JOIN EnvEventGrid g ON g.envEventId = a.id
        WHERE g.gridId = :gridId
        AND g.enabled = TRUE
    """)
    List<EnvEvent> findByGridId(@Param("gridId") String gridId);

    @Query("""
        SELECT DISTINCT a FROM EnvEvent a
        WHERE a.crossRegion = TRUE
    """)
    List<EnvEvent> findCrossRegion();

    @Query("""
        SELECT DISTINCT a FROM EnvEvent a
        WHERE a.eventType = :eventType
        ORDER BY a.createTime DESC
    """)
    List<EnvEvent> findByEventType(@Param("eventType") String eventType);
}
