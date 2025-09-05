package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.EnvServiceInfo;
import edu.fudan.se.sctap_lowcode_tool.service.EnvServiceInfoService;
import edu.fudan.se.sctap_lowcode_tool.DTO.EnvServiceInfoRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/env-services")
@RequiredArgsConstructor
public class EnvServiceInfoController {

    private final EnvServiceInfoService envServiceInfoService;

    @PostMapping
    public ResponseEntity<EnvServiceInfo> create(@RequestBody EnvServiceInfoRequest req) {
        EnvServiceInfo created = envServiceInfoService.create(req.getEnvServiceName(), req.getProjectId());
        return ResponseEntity.created(URI.create("/api/env-services/" + created.getId())).body(created);
    }

    @GetMapping("/{id}")
    public EnvServiceInfo get(@PathVariable Integer id) {
        return envServiceInfoService.getById(id);
    }

    @GetMapping
    public List<EnvServiceInfo> list(@RequestParam(value = "projectId", required = false) Integer projectId) {
        if (projectId != null) {
            return envServiceInfoService.listByProject(projectId);
        }
        return envServiceInfoService.listAll();
    }

    @PutMapping("/{id}")
    public EnvServiceInfo update(@PathVariable Integer id, @RequestBody EnvServiceInfoRequest req) {
        return envServiceInfoService.update(id, req.getEnvServiceName(), req.getProjectId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        envServiceInfoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }
}