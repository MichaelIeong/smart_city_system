package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.SpaceNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.SpaceNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SpaceService {

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private SpaceNodeRepository spaceNodeRepository;

    /**
     * 根据项目ID查询所有空间
     */
    public List<SpaceInfo> findSpacesByProjectId(int projectId) {
        return spaceRepository.findByProjectInfo_ProjectId(projectId);
    }

    /**
     * 根据空间ID查询具体空间
     */
    public Optional<SpaceInfo> findSpaceById(int id) {
        return spaceRepository.findById(id);
    }

    /**
     * 根据ProjectInfo查询所有空间
     */
    public List<SpaceInfo> findByProjectInfo(ProjectInfo projectInfo) {
        return spaceRepository.findByProjectInfo(projectInfo);
    }

    /**
     * 根据空间名称查询SpaceInfo
     */
    public Optional<SpaceInfo> findBySpaceName(String spaceName) {
        return spaceRepository.findBySpaceName(spaceName);
    }

    /**
     * 保存空间并同步 Neo4j
     */
    public SpaceInfo saveSpace(SpaceInfo spaceInfo) {
        SpaceInfo saved = spaceRepository.save(spaceInfo);

        SpaceNode node = new SpaceNode();
        node.setId(saved.getId().longValue());
        node.setSpaceId(saved.getSpaceId());
        node.setSpaceName(saved.getSpaceName());
        node.setFixedProperties(saved.getFixedProperties());
        node.setDescription(saved.getDescription());

        spaceNodeRepository.save(node);
        return saved;
    }

    /**
     * 更新空间并同步 Neo4j
     */
    public Optional<SpaceInfo> updateSpace(Integer id, SpaceInfo updated) {
        return spaceRepository.findById(id).map(existing -> {
            existing.setSpaceId(updated.getSpaceId());
            existing.setSpaceName(updated.getSpaceName());
            existing.setFixedProperties(updated.getFixedProperties());
            existing.setDescription(updated.getDescription());
            SpaceInfo saved = spaceRepository.save(existing);

            spaceNodeRepository.findBySpaceId(saved.getSpaceId()).ifPresentOrElse(node -> {
                node.setSpaceName(saved.getSpaceName());
                node.setFixedProperties(saved.getFixedProperties());
                node.setDescription(saved.getDescription());
                spaceNodeRepository.save(node);
            }, () -> {
                SpaceNode newNode = new SpaceNode();
                newNode.setSpaceId(saved.getSpaceId());
                newNode.setSpaceName(saved.getSpaceName());
                newNode.setFixedProperties(saved.getFixedProperties());
                newNode.setDescription(saved.getDescription());
                spaceNodeRepository.save(newNode);
            });

            return saved;
        });
    }

    /**
     * 删除空间并同步 Neo4j
     */
    public void deleteSpace(int id) {
        spaceRepository.findById(id).ifPresent(space -> {
            spaceNodeRepository.findBySpaceId(space.getSpaceId())
                               .ifPresent(node -> spaceNodeRepository.deleteById(node.getId()));
        });
        spaceRepository.deleteById(id);
    }
}