package edu.fudan.se.sctap_lowcode_tool.controller;
import edu.fudan.se.sctap_lowcode_tool.model.GridMesh;
import edu.fudan.se.sctap_lowcode_tool.service.GridService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grid")
public class GridController {

    @Autowired
    private GridService gridService;

    /**
     * 获取网格的全部信息，包括元信息、设备信息、环境级事件、属性、服务、应用
     * */
    @GetMapping("/{meshId}")
    public Map<String, Object> getGridInfo(@PathVariable String meshId) {
        return gridService.getGridDetail(meshId);
    }

    /**
     * 获取网格的基本信息
     * */
    @GetMapping("/base/{gridId}")
    public ResponseEntity<GridMesh> getGridById(@PathVariable String gridId) {
        return ResponseEntity.ok(gridService.getGridById(gridId));
    }

    /**
     * 获取同类型的网格列表
     */
    @GetMapping("/type/{gridId}")
    public ResponseEntity<List<GridMesh>> getGridListByType(@PathVariable String gridId) {
        return ResponseEntity.ok(gridService.getGridListByType(gridId));
    }

    /**
     * 根据应用id获取同类型的网格
     * */
    @GetMapping("/typeOfApp/{appId}")
    public ResponseEntity<List<GridMesh>> getGridListByAppId(@PathVariable Integer appId) {
        return ResponseEntity.ok(gridService.getGridListByAppId(appId));
    }
}