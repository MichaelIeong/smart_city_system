package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.CyberResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SocialResourceInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CyberResourceRepository extends JpaRepository<CyberResourceInfo, Integer> {
    List<CyberResourceInfo> findByProjectInfoProjectId(Integer projectId);

    CyberResourceInfo findByResourceId(String resourceId);

    List<CyberResourceInfo> findByResourceType(String resourceType);
}
