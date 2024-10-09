package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.SocialResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.service.SocialResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/socialResources")
public class SocialResourceController {

    @Autowired
    private SocialResourceService socialResourceService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<SocialResourceInfo>> getSocialResourcesByProjectId(@PathVariable Integer projectId) {
        return ResponseEntity.ok(socialResourceService.getSocialResourceByProjectId(projectId));
    }
}
