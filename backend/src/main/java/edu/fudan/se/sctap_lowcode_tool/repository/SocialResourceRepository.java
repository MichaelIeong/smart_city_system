package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.SocialResourceInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SocialResourceRepository extends JpaRepository<SocialResourceInfo, Integer> {
    List<SocialResourceInfo> findByProjectInfoProjectId(Integer projectId);
}
