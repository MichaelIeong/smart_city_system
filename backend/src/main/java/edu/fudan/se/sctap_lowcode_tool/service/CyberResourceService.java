package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.CyberResourceRequest;
import edu.fudan.se.sctap_lowcode_tool.model.CyberResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.CyberResourceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CyberResourceService {

    @Autowired
    private CyberResourceRepository cyberResourceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public List<CyberResourceInfo> getCyberResourceByProjectId(Integer projectId) {
        return cyberResourceRepository.findByProjectInfoProjectId(projectId);
    }

    public CyberResourceInfo findByResourceId(String resourceId) {
        return cyberResourceRepository.findByResourceId(resourceId);
    }

    public CyberResourceInfo createCyberResource(Integer projectId, CyberResourceRequest cyberResourceRequest) {
        CyberResourceInfo cyberResourceInfo = new CyberResourceInfo();
        Optional<ProjectInfo> projectInfo = projectRepository.findById(projectId);
        projectInfo.ifPresent(cyberResourceInfo::setProjectInfo);
        cyberResourceInfo.setResourceId(cyberResourceRequest.resourceId());
        cyberResourceInfo.setResourceType(cyberResourceRequest.resourceType());
        cyberResourceInfo.setDescription(cyberResourceRequest.description());
        cyberResourceInfo.setUrl(cyberResourceRequest.url());
        return cyberResourceRepository.save(cyberResourceInfo);
    }
}
