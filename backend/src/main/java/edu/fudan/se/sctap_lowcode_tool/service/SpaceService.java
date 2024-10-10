package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpaceService {

    @Autowired
    private SpaceRepository spaceRepository;

    public List<SpaceInfo> findSpacesByProjectId(int projectId) {
        return spaceRepository.findByProjectInfo_ProjectId(projectId);
    }

}