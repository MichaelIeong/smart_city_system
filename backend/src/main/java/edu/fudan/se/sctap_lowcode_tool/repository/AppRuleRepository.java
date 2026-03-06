package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppRuleRepository extends JpaRepository<AppRuleInfo, Integer> {
    @Query("SELECT a FROM AppRuleInfo a WHERE a.project.projectId = :projectId ORDER BY a.id ASC")
    Page<AppRuleInfo> findAllByProjectId(@Param("projectId") Integer projectId, Pageable pageable);

    Optional<AppRuleInfo> findById(int id);

    @Query("""
        SELECT DISTINCT a FROM AppRuleInfo a
        JOIN AppGrid g ON g.appRuleId = a.id
        WHERE a.eventType = :eventType
        AND g.gridId = :location
        AND g.enabled = TRUE
        ORDER BY a.updateTime DESC
    """)
    List<AppRuleInfo> findByEventTypeAndLocation(
            @Param("eventType") String eventType,
            @Param("location") String location);


    @Query("""
        SELECT a FROM AppRuleInfo a
        WHERE a.project.projectId = :projectId
          AND (:eventType IS NULL OR :eventType = '' OR a.eventType = :eventType)
          AND (:description IS NULL OR :description = ''
               OR LOWER(a.description) LIKE LOWER(CONCAT('%', :description, '%')))
    """)
    Page<AppRuleInfo> searchByProjectWithFilters(
            @Param("projectId") Integer projectId,
            @Param("eventType") String eventType,
            @Param("description") String description,
            Pageable pageable);

    @Query("SELECT r FROM AppRuleInfo r JOIN AppGrid g ON r.id = g.appRuleId WHERE g.gridId = :gridId")
    List<AppRuleInfo> findByGridId(@Param("gridId") String gridId);

    @Query("SELECT a FROM AppRuleInfo a WHERE a.crossRegion = TRUE ORDER BY a.updateTime DESC")
    List<AppRuleInfo> findAllGlobalRules();

    @Query("""
        SELECT DISTINCT a FROM AppRuleInfo a
        WHERE a.eventType = :eventType
        AND a.crossRegion = TRUE
        ORDER BY a.updateTime DESC
    """)
    List<AppRuleInfo> findByEventTypeAndCrossRegion(@Param("eventType") String eventType);

    @Modifying
    @Query("DELETE FROM AppRuleInfo a WHERE a.project.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Integer projectId);

    @Query("SELECT a.id FROM AppRuleInfo a WHERE a.project.projectId = :projectId")
    List<Integer> findIdsByProjectId(@Param("projectId") Integer projectId);

    @Query("SELECT a FROM AppRuleInfo a WHERE a.crossRegion = TRUE AND a.project.projectId = :projectId")
    List<AppRuleInfo> findGlobalRulesByProject(@Param("projectId") Integer projectId);
}
