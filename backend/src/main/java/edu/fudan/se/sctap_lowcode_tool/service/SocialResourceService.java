package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.SocialResourceRequest;
import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SocialResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.ProjectRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.SocialResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SocialResourceService {

    @Autowired
    private SocialResourceRepository socialResourceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public List<SocialResourceInfo> getSocialResourceByProjectId(Integer projectId) {
        return socialResourceRepository.findByProjectInfoProjectId(projectId);
    }

    public SocialResourceInfo findByResourceId(String resourceId) {
        return socialResourceRepository.findByResourceId(resourceId);
    }

    public SocialResourceInfo createSocialResource(Integer projectId, SocialResourceRequest socialResourceRequest) {
        SocialResourceInfo socialResourceInfo = new SocialResourceInfo();
        Optional<ProjectInfo> projectInfo = projectRepository.findById(projectId);
        projectInfo.ifPresent(socialResourceInfo::setProjectInfo);
        socialResourceInfo.setResourceId(socialResourceRequest.resourceId());
        socialResourceInfo.setResourceType(socialResourceRequest.resourceType());
        socialResourceInfo.setDescription(socialResourceRequest.description());
        socialResourceInfo.setUrl(socialResourceRequest.url());
        return socialResourceRepository.save(socialResourceInfo);
    }
}
