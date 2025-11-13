package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvEventRepository extends JpaRepository<EnvEvent, Integer> {
    List<EnvEvent> findByGridId(String gridId);
}
