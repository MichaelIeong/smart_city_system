package edu.fudan.se.sctap_lowcode_tool.controller;
import edu.fudan.se.sctap_lowcode_tool.service.GridService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grid")
@CrossOrigin
public class GridController {

    @Autowired
    private GridService gridService;

    @GetMapping("/{meshId}")
    public Map<String, Object> getGridInfo(@PathVariable String meshId) {
        return gridService.getGridDetail(meshId);
    }

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
}