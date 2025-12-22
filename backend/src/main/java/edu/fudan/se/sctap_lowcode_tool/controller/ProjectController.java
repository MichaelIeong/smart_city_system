package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.DTO.SceneImportRequest;
import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.ProjectRepository;
import edu.fudan.se.sctap_lowcode_tool.service.ImportService;
import edu.fudan.se.sctap_lowcode_tool.service.ProjectService;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.ImportFileParser;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.ParseException;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.UnZip;
import edu.fudan.se.sctap_lowcode_tool.model.MeshInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets; // 导入用于读取文件内容的工具

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    private ProjectRepository projectRepository;

    @PostMapping("/upload")
    @Operation(summary = "上传项目信息", description = "上传新的或更新现有项目的信息。")
    public ResponseEntity<ProjectInfo> postProjects(@RequestBody ProjectInfo projectInfo) {
        return ResponseEntity.ok(projectService.saveOrUpdateProject(projectInfo));
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

    @PostMapping("/importJson")
    public ResponseEntity<String> importJson(@RequestBody SceneImportRequest dto) {
        Integer id = importService.importJsonProject(dto);
        return ResponseEntity.ok("项目导入成功，项目ID=" + id);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Integer projectId) {
        if (projectId <= 3) {
            return ResponseEntity.badRequest().body("不可删除演示项目");
        }
        // 执行删除逻辑，建议在 Service 层同时清除关联的 MeshInfo
        projectRepository.deleteById(projectId);
        return ResponseEntity.ok("项目已成功删除");
    }

    @Operation(summary = "新增场景：上传JSON文件", description = "接收场景JSON文件和项目名称，直接解析并导入数据库。")
    @PostMapping("/importSceneFile")
    public ResponseEntity<String> importSceneFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sceneName") String sceneName // 假设前端传递的是场景名称
    ) {
        if (file.isEmpty()) {
            throw new BadRequestException(new RuntimeException("上传文件内容为空。"));
        }

        try {
            // 1. 读取 MultipartFile 的内容为字符串
            String jsonContent = new String(file.getBytes(), StandardCharsets.UTF_8);

            // 2. 解析原始JSON内容，获取 MeshInfo 列表
            // F-park.json 的结构是 { "data": [ { "meshInfo": {...} }, ... ] }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonContent);
            JsonNode dataNode = rootNode.path("data");

            List<MeshInfo> meshes = new ArrayList<>();
            if (dataNode.isArray()) {
                for (JsonNode dataItem : dataNode) {
                    JsonNode meshInfoNode = dataItem.path("meshInfo");
                    if (!meshInfoNode.isMissingNode()) {
                        // 将 meshInfo 节点映射为 MeshInfo DTO/Model
                        MeshInfo mesh = mapper.treeToValue(meshInfoNode, MeshInfo.class);
                        meshes.add(mesh);
                    }
                }
            } else {
                throw new BadRequestException(new RuntimeException("JSON文件结构错误，未找到'data'数组。"));
            }

            // 3. 构造 SceneImportRequest DTO，将解析出的数据传入
            SceneImportRequest dto = new SceneImportRequest();
            dto.setProjectName(sceneName); // 场景名称映射为项目名称
            dto.setMeshes(meshes);         // 传入解析后的 Mesh 列表

            // 4. 调用 Service 导入逻辑
            Integer id = importService.importJsonProject(dto);

            return ResponseEntity.ok("场景导入成功，项目ID=" + id);

        } catch (IOException e) {
            log.error("File processing failed.", e);
            throw new BadRequestException(new RuntimeException("文件读取失败或JSON格式错误：" + e.getMessage()));
        } catch (Exception e) {
            log.error("Scene import failed during service processing.", e);
            throw new BadRequestException(new RuntimeException("场景数据导入失败：" + e.getMessage()));
        }
    }

}