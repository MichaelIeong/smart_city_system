package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.service.ImportService;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.MetaBFSIterator;
import edu.fudan.se.sctap_lowcode_tool.utils.import_utils.UnZip;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

// TODO: remove this file, move the method into FileController

@RestController
@RequestMapping("/api/import")
@Tag(name = "ImportController", description = "导入管理控制器")
public class ImportController {

    private static final Logger log = LoggerFactory.getLogger(ImportController.class);

    @Autowired
    private ImportService importService;

    @Operation(summary = "导入特斯联提供的模型文件(ZIP压缩包)", description = "该服务将增加device, device_type, space, event到数据库中")
    @PostMapping("/upload")
    public ResponseEntity<String> postEvent(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectName") String projectName
    ) {
        try {
            // unzip
            Path destDir = Path.of("src/main/resources/unzip-" + UUID.randomUUID());
            UnZip.unzip(file, destDir);

            // iterate the unzip files
            MetaBFSIterator iterator = MetaBFSIterator.usingIndex(destDir);
            importService.importMetaRecursively(iterator, projectName);

            // delete the unzip files
            if (!UnZip.deleteDirectory(destDir)) {
                log.warn("Failed to delete the unzip files - {}", destDir);
            }

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("Failed to import the file - {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
