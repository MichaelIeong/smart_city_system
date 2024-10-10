package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SpaceService {

    @Autowired
    private SpaceRepository spaceRepository;

    public List<SpaceInfo> findSpacesByProjectId(int projectId) {
        return spaceRepository.findByProjectInfo_ProjectId(projectId);
    }

    public Optional<SpaceInfo> findSpaceById(int id) {
        return spaceRepository.findById(id);
    }

    public List<SpaceInfo> findByProjectInfo(ProjectInfo projectInfo){
        return spaceRepository.findByProjectInfo(projectInfo);
    }
}