package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.CyberResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.CyberResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CyberResourceService {

    @Autowired
    private CyberResourceRepository cyberResourceRepository;

    public List<CyberResourceInfo> getCyberResourceByProjectId(Integer projectId) {
        return cyberResourceRepository.findByProjectInfoProjectId(projectId);
    }

    public CyberResourceInfo findByResourceId(String resourceId) {
        return cyberResourceRepository.findByResourceId(resourceId);
    }
}
