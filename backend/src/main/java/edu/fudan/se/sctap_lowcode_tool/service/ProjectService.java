package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.ProjectRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private JsonUtil jsonUtil;

    /**
     * 保存或更新项目。
     *
     * @param projectInfo 项目信息
     * @return 保存或更新后的项目信息
     */
    public ProjectInfo saveOrUpdateProject(ProjectInfo projectInfo) {
        return projectRepository.save(projectInfo);
    }

    /**
     * 根据ID删除项目。
     *
     * @param projectId 项目ID
     * @return 删除成功返回true，否则返回false
     */
    public boolean deleteProjectById(int projectId) {
        if (projectRepository.existsById(projectId)) {
            projectRepository.deleteById(projectId);
            return true;
        }
        return false;
    }

    /**
     * 根据ID查找项目。
     *
     * @param projectId 项目ID
     * @return 项目信息
     */
    public Optional<ProjectInfo> findById(int projectId) {
        return Optional.ofNullable(projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found")));
    }

    /**
     * 根据ID获取项目名称。
     *
     * @param projectId 项目ID
     * @return 项目名称
     */
    public String getProjectName(int projectId) {
        return projectRepository.findById(projectId)
                .map(ProjectInfo::getProjectName)
                .orElse("Project not found");
    }

    /**
     * 获取所有项目。
     *
     * @return 所有项目信息
     */
    public Iterable<ProjectInfo> findAll() {
        return Optional.of(projectRepository.findAll())
                .orElseGet(Collections::emptyList);
    }

    /**
     * 导入项目（从JSON字符串）。
     *
     * @param json 项目JSON字符串
     * @return 导入成功返回true，否则返回false
     */
    public boolean importProjects(String json) {
        return Optional.ofNullable(json)
                .map(j -> jsonUtil.parseJsonToList(j, ProjectInfo.class))
                .map(projects -> {
                    projects.forEach(this::saveOrUpdateProject);
                    return true;
                })
                .orElse(false);
    }

    /**
     * 导出所有项目为JSON字符串。
     *
     * @return JSON字符串
     */
    public Optional<String> exportProjects() {
        return Optional.ofNullable(findAll())
                .map(projects -> jsonUtil.convertListToJson((List<ProjectInfo>) projects));
    }

}