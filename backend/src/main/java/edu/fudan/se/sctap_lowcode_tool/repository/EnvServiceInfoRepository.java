package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.EnvServiceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnvServiceInfoRepository extends JpaRepository<EnvServiceInfo, Integer> {
    List<EnvServiceInfo> findByProject(ProjectInfo project);
}