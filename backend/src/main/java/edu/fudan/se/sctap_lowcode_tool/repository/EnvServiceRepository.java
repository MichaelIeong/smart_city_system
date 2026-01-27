package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    //按服务名称查找
    EnvService findByServiceName(String serviceName);

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
    """)
    Page<EnvService> searchWithFilters(
            @Param("name") String name,
            @Param("description") String description,
            Pageable pageable);
}
