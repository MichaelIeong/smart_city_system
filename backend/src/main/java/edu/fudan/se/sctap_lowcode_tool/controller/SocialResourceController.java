package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.SocialResourceRequest;
import edu.fudan.se.sctap_lowcode_tool.model.SocialResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.service.SocialResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/socialResources")
public class SocialResourceController {

    @Autowired
    private SocialResourceService socialResourceService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<SocialResourceInfo>> getSocialResourcesByProjectId(@PathVariable Integer projectId) {
        System.out.println(socialResourceService.getSocialResourceByProjectId(projectId));
        return ResponseEntity.ok(socialResourceService.getSocialResourceByProjectId(projectId));
    }

    @PostMapping("/project/{projectId}")
    public ResponseEntity<SocialResourceInfo> createSocialResourceForProject(
            @PathVariable Integer projectId,
            @RequestBody SocialResourceRequest socialResourceRequest) {
            SocialResourceInfo createdResource = socialResourceService.createSocialResource(projectId, socialResourceRequest);
            return ResponseEntity.ok(createdResource);
    }
}
