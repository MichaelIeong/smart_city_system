package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.CyberResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.service.CyberResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cyberResources")
public class CyberResourceController {

    @Autowired
    private CyberResourceService cyberResourceService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<CyberResourceInfo>> getCyberResourcesByProjectId(@PathVariable Integer projectId) {
        return ResponseEntity.ok(cyberResourceService.getCyberResourceByProjectId(projectId));
    }

}
