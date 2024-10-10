package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import edu.fudan.se.sctap_lowcode_tool.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "ProjectController", description = "项目控制器")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping("/upload")
    @Operation(summary = "上传项目信息", description = "上传新的或更新现有项目的信息。")
    public ResponseEntity<ProjectInfo> postProjects(@RequestBody ProjectInfo projectInfo) {
        return ResponseEntity.ok(projectService.saveOrUpdateProject(projectInfo));
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "删除项目", description = "删除指定的项目。")
    public ResponseEntity<Void> deleteProject(@PathVariable int projectId) {
        return projectService.deleteProjectById(projectId) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "更新项目信息", description = "更新指定项目的详细信息。")
    public ResponseEntity<Void> updateProject(@PathVariable int projectId, @RequestBody ProjectInfo projectInfo) {
        projectInfo.setProjectId(projectId);
        projectService.saveOrUpdateProject(projectInfo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "获取项目信息", description = "根据项目Id获取项目的详细信息。")
    public ResponseEntity<ProjectInfo> getProjectById(@PathVariable int projectId) {
        return projectService.findById(projectId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{projectId}/name")
    @Operation(summary = "查询项目名称", description = "获取指定项目的名称。")
    public ResponseEntity<String> getProjectName(@PathVariable int projectId) {
        String projectName = projectService.getProjectName(projectId);
        return projectName != null ? ResponseEntity.ok(projectName) : ResponseEntity.notFound().build();
    }

    @GetMapping("/allProjects")
    @Operation(summary = "获取所有项目信息", description = "获取所有项目的详细信息。")
    public ResponseEntity<Iterable<ProjectInfo>> getAllProjects() {
        return ResponseEntity.ok(projectService.findAll());
    }

    @PostMapping("/import")
    @Operation(summary = "导入项目信息", description = "从JSON文件导入项目信息。")
    public ResponseEntity<Void> importProjects(@RequestBody String json) {
        boolean isSuccess = projectService.importProjects(json);
        return isSuccess ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @GetMapping("/export")
    @Operation(summary = "导出项目信息", description = "导出所有项目信息为JSON文件。")
    public ResponseEntity<String> exportProjects() {
        return projectService.exportProjects()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(500).body("Error generating JSON"));
    }
}