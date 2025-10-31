package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.CyberResourceRequest;
import edu.fudan.se.sctap_lowcode_tool.model.CyberResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.service.CyberResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cyberResources")
public class CyberResourceController {

    @Autowired
    private CyberResourceService cyberResourceService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<CyberResourceInfo>> getCyberResourcesByProjectId(@PathVariable Integer projectId) {
        System.out.println(cyberResourceService.getCyberResourceByProjectId(projectId));
        return ResponseEntity.ok(cyberResourceService.getCyberResourceByProjectId(projectId));
    }

    @PostMapping("/project/{projectId}")
    public ResponseEntity<CyberResourceInfo> createCyberResourceForProject(
            @PathVariable Integer projectId,
            @RequestBody CyberResourceRequest request) {
        return ResponseEntity.ok(cyberResourceService.createCyberResource(projectId, request));
    }

}
