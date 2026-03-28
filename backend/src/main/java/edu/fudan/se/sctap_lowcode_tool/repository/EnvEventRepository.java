package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Query("""
        SELECT a FROM EnvEvent a
        WHERE (:eventType IS NULL OR :eventType = '' OR a.eventType = :eventType)
          AND (:eventName IS NULL OR :eventName = ''
               OR LOWER(a.eventName) LIKE LOWER(CONCAT('%', :eventName, '%')))
          AND (:projectId IS NULL OR a.projectId = :projectId)
    """)
    Page<EnvEvent> searchWithFilters(
            @Param("eventType") String eventType,
            @Param("eventName") String eventName,
            @Param("projectId") Integer projectId,
            Pageable pageable);

    @Modifying
    void deleteByProjectId(Integer projectId);

    @Query("SELECT e.id FROM EnvEvent e WHERE e.projectId = :projectId")
    List<Integer> findIdsByProjectId(@Param("projectId") Integer projectId);

    @Query("""
        SELECT DISTINCT a FROM EnvEvent a
        WHERE a.crossRegion = TRUE
          AND (:projectId IS NULL OR a.projectId = :projectId)
    """)
    List<EnvEvent> findCrossRegionByProject(@Param("projectId") Integer projectId);

    List<EnvEvent> findByProjectId(Integer projectId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO env_event (id, event_type, description, event_json, rule_dsl, event_name, cross_region, create_time, project_id, depend_dtypes) " +
            "VALUES (:#{#e.id}, :#{#e.eventType}, :#{#e.description}, :#{#e.eventJson}, :ruleDslStr, :#{#e.eventName}, :#{#e.crossRegion}, :#{#e.createTime}, :#{#e.projectId}, :dependDtypesStr)",
            nativeQuery = true)
    void insertWithId(@Param("e") EnvEvent e,
                      @Param("ruleDslStr") String ruleDslStr,
                      @Param("dependDtypesStr") String dependDtypesStr);
}
