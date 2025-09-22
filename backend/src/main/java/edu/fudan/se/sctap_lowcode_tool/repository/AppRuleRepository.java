package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppRuleRepository extends JpaRepository<AppRuleInfo, Integer> {
    @Query("SELECT a FROM AppRuleInfo a WHERE a.project.projectId = :projectId ORDER BY a.id ASC")
    Page<AppRuleInfo> findAllByProjectId(@Param("projectId") Integer projectId, Pageable pageable);

    Optional<AppRuleInfo> findById(int id);

    @Query("""
        SELECT a FROM AppRuleInfo a WHERE a.eventType = :eventType
            AND a.project.projectId = :projectId
            ANd a.enabled = :enabled
    """)
    List<AppRuleInfo> findByEventTypeAndProjectId(
            @Param("eventType") String eventType,
            @Param("projectId") Integer projectId,
            @Param("enabled") Boolean enabled);


    @Query("""
        SELECT a FROM AppRuleInfo a
        WHERE a.project.projectId = :projectId
          AND (:eventType IS NULL OR :eventType = '' OR a.eventType = :eventType)
          AND (:description IS NULL OR :description = ''
               OR LOWER(a.description) LIKE LOWER(CONCAT('%', :description, '%')))
        ORDER BY a.id ASC
    """)
    Page<AppRuleInfo> searchByProjectWithFilters(
            @Param("projectId") Integer projectId,
            @Param("eventType") String eventType,
            @Param("description") String description,
            Pageable pageable);

}
