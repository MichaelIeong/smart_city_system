package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EnvEventGrid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvEventGridRepository extends JpaRepository<EnvEventGrid, Integer> {
    List<EnvEventGrid> findByEnvEventId(Integer envEventId);

    @Modifying
    @Query("DELETE FROM EnvEventGrid g WHERE g.envEventId IN ?1")
    void deleteByEnvEventIdIn(List<Integer> envEventIds);
}
