package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import edu.fudan.se.sctap_lowcode_tool.service.ImportService;
import edu.fudan.se.sctap_lowcode_tool.service.ProjectService;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.ImportFileParser;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.ParseException;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.UnZip;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@Tag(name = "ProjectController", description = "项目控制器")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ImportService importService;

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

    @Operation(summary = "导入特斯联提供的模型文件(ZIP压缩包)", description = "该服务将增加device, device_type, space, event到数据库中")
    @PostMapping("/import")
    public ResponseEntity<String> postEvent(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectName") String projectName
    ) {
        Path destDir = Path.of("src/main/resources/unzip-" + UUID.randomUUID());
        try {
            // unzip
            UnZip.unzip(file, destDir);

            // iterate the unzip files
            var tree = ImportFileParser.parseMetaTree(destDir);
            importService.importRecursively(tree, projectName);

        } catch (IOException | ParseException e) {
            throw new BadRequestException(e);
        } finally {
            // delete the unzip files
            if (!UnZip.deleteDirectory(destDir)) {
                log.warn("Failed to delete the unzip files - {}", destDir);
            }
        }

        return ResponseEntity.ok("ok");
    }

    @GetMapping("/export")
    @Operation(summary = "导出项目信息", description = "导出所有项目信息为JSON文件。")
    public ResponseEntity<String> exportProjects() {
        return projectService.exportProjects()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(500).body("Error generating JSON"));
    }
}