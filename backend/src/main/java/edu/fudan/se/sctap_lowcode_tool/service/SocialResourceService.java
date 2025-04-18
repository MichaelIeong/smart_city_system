package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.SocialResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.SocialResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SocialResourceService {

    @Autowired
    private SocialResourceRepository socialResourceRepository;

    public List<SocialResourceInfo> getSocialResourceByProjectId(Integer projectId) {
        return socialResourceRepository.findByProjectInfoProjectId(projectId);
    }

    public SocialResourceInfo findByResourceId(String resourceId) {
        return socialResourceRepository.findByResourceId(resourceId);
    }
}
