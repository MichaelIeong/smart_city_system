package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.PropertyBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @GetMapping
    public ResponseEntity<Iterable<PropertyBriefResponse>> getPropertiesByProjectId(
            @RequestParam(name = "project") int projectId) {
        return ResponseEntity.ok(propertyService.findAllByProjectId(projectId));
    }

}
