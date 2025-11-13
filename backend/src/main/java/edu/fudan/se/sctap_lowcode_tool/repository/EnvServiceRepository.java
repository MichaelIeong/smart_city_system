package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvServiceRepository extends JpaRepository<EnvService, Integer> {
    List<EnvService> findByGridId(String gridId);
}
