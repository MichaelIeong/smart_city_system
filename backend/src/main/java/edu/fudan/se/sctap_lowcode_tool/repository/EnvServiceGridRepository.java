package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EnvServiceGrid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvServiceGridRepository extends JpaRepository<EnvServiceGrid, Integer> {
    List<EnvServiceGrid> findByEnvServiceId(Integer envServiceId);

    @Modifying
    @Query("DELETE FROM EnvServiceGrid g WHERE g.envServiceId IN ?1")
    void deleteByEnvServiceIdIn(List<Integer> envServiceIds);
}
