package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    //按服务名称查找
    EnvService findByServiceName(String serviceName);

    @Query("""
        SELECT e FROM EnvService e
        WHERE e.serviceName = :serviceName
        ORDER BY e.createTime DESC
        LIMIT 1
    """)
    EnvService findLatestByServiceName(@Param("serviceName") String serviceName);

    @Query("""
        SELECT DISTINCT a FROM EnvService a
        WHERE a.crossRegion = TRUE
    """)
    List<EnvService> findCrossRegion();

    @Query("""
        SELECT a FROM EnvService a
        WHERE (:name IS NULL OR :name = ''
               OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:description IS NULL OR :description = ''
               OR LOWER(a.description) LIKE LOWER(CONCAT('%', :description, '%')))
          AND (:projectId IS NULL OR a.projectId = :projectId)
    """)
    Page<EnvService> searchWithFilters(
            @Param("name") String name,
            @Param("description") String description,
            @Param("projectId") Integer projectId,
            Pageable pageable);

    @Modifying
    void deleteByProjectId(Integer projectId);

    @Query("SELECT s.id FROM EnvService s WHERE s.projectId = :projectId")
    List<Integer> findIdsByProjectId(@Param("projectId") Integer projectId);

    @Query("""
        SELECT DISTINCT s FROM EnvService s
        WHERE s.crossRegion = TRUE
          AND (:projectId IS NULL OR s.projectId = :projectId)
    """)
    List<EnvService> findCrossRegionByProject(@Param("projectId") Integer projectId);

}
