package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.service.GridService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meshes")
public class MeshController {

    @Autowired
    private GridService gridService;

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllGrids() {
        List<Map<String, Object>> grids = gridService.getAllGridList();
        return ResponseEntity.ok(grids);
    }
}