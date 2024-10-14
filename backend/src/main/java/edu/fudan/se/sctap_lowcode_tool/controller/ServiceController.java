package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @GetMapping
    public ResponseEntity<Iterable<ServiceBriefResponse>> getServicesByProjectId(
            @RequestParam(name = "project") int projectId) {
        return ResponseEntity.ok(serviceService.findAllByProjectId(projectId));
    }
}
