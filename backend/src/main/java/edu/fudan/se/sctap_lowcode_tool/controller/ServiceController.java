package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.service.ServiceService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "上传新的服务", description = "用户在node-red组合好服务，传给后端，加入到数据库")
    @PostMapping("/uploadservice")
    public ResponseEntity<Void> saveService(@RequestBody JsonNode serviceMsg) {
        System.out.println(serviceMsg);
        return ResponseEntity.ok().build();
    }
}
