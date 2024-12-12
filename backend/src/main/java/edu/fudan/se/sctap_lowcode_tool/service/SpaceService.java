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

    /**
     * 根据项目ID查询所有空间
     *
     * @param projectId 项目ID
     * @return 空间列表
     */
    public List<SpaceInfo> findSpacesByProjectId(int projectId) {
        return spaceRepository.findByProjectInfo_ProjectId(projectId);
    }

    /**
     * 根据空间ID查询具体空间
     *
     * @param id 空间ID
     * @return 空间信息（Optional包装）
     */
    public Optional<SpaceInfo> findSpaceById(int id) {
        return spaceRepository.findById(id);
    }

    /**
     * 根据ProjectInfo查询所有空间
     *
     * @param projectInfo 项目信息
     * @return 空间列表
     */
    public List<SpaceInfo> findByProjectInfo(ProjectInfo projectInfo) {
        return spaceRepository.findByProjectInfo(projectInfo);
    }

    /**
     * 根据空间名称查询SpaceInfo
     *
     * @param spaceName 空间名称
     * @return 空间信息（Optional包装）
     */
    public Optional<SpaceInfo> findBySpaceName(String spaceName) {
        return spaceRepository.findBySpaceName(spaceName);
    }
}