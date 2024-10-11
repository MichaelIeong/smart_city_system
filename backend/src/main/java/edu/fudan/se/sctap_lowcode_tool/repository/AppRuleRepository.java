package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AppRuleRepository extends JpaRepository<AppRuleInfo, Integer> {
    @Query("SELECT a FROM AppRuleInfo a WHERE a.project.projectId = :projectId ORDER BY a.id ASC")
    Page<AppRuleInfo> findAllByProjectId(@Param("projectId") Integer projectId, Pageable pageable);

    Optional<AppRuleInfo> findById(int id);

}
