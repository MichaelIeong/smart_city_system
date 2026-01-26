package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EnvEventGrid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvEventGridRepository extends JpaRepository<EnvEventGrid, Integer> {
    List<EnvEventGrid> findByEnvEventId(Integer envEventId);
}
