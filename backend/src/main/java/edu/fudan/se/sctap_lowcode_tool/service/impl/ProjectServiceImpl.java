package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.ProjectRepository;
import edu.fudan.se.sctap_lowcode_tool.service.ProjectService;
import edu.fudan.se.sctap_lowcode_tool.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private JsonUtil jsonUtil;

    @Override
    public ProjectInfo saveOrUpdateProject(ProjectInfo projectInfo) {
        // 保存或更新项目
        return projectRepository.save(projectInfo);
    }

    @Override
    public boolean deleteProjectById(int projectId) {
        // 根据ID删除项目
        if (projectRepository.existsById(projectId)) {
            projectRepository.deleteById(projectId);
            return true;
        }
        return false;
    }

    @Override
    public Optional<ProjectInfo> findById(int projectId) {
        // 根据ID查找项目
        return Optional.ofNullable(projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found")));
    }

    @Override
    public String getProjectName(int projectId) {
        // 根据ID获取项目名称
        return projectRepository.findById(projectId)
                .map(ProjectInfo::getProjectName)
                .orElse("Project not found");
    }

    @Override
    public Iterable<ProjectInfo> findAll() {
        // 获取所有项目
        return Optional.of(projectRepository.findAll())
                .orElseGet(Collections::emptyList);
    }

    @Override
    public boolean importProjects(String json) {
        // 导入项目（从JSON字符串）
        return Optional.ofNullable(json)
                .map(j -> jsonUtil.parseJsonToList(j, ProjectInfo.class))
                .map(projects -> {
                    projects.forEach(this::saveOrUpdateProject);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public Optional<String> exportProjects() {
        // 导出所有项目为JSON字符串
        return Optional.ofNullable(findAll())
                .map(projects -> jsonUtil.convertListToJson((List<ProjectInfo>) projects));
    }

}