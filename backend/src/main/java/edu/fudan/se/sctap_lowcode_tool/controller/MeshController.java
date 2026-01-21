package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.service.TslDeviceService;
// 如果您原有代码中有 MeshService，请保留引入，没有则忽略
// import edu.fudan.se.sctap_lowcode_tool.service.MeshService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meshes")
public class MeshController {

    @Autowired
    private TslDeviceService tslDeviceService;

    /**
     * 获取网格列表
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllGrids(@RequestParam(value = "mesh_nature", required = false) String meshNature) {
        if (meshNature != null && !meshNature.isEmpty()) {
            return ResponseEntity.ok(tslDeviceService.getMeshesByScene(meshNature));
        }

        return ResponseEntity.ok(Collections.emptyList());
    }
}