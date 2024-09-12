package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;

import java.util.Optional;

public interface ProjectService {

    ProjectInfo saveOrUpdateProject(ProjectInfo projectInfo); // 保存或更新项目

    boolean deleteProjectById(int projectId); // 根据ID删除项目

    Optional<ProjectInfo> findById(int projectId); // 根据ID查找项目

    String getProjectName(int projectId);

    Iterable<ProjectInfo> findAll(); // 获取所有项目

    // todo: 项目预览图（从model studio获取）
    String getProjectPreview(int projectId);

    boolean importProjects(String json);    // 导入项目

    Optional<String> exportProjects();  // 导出项目
}