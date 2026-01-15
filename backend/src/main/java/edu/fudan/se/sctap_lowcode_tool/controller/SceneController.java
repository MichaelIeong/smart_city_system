package edu.fudan.se.sctap_lowcode_tool.controller;
import edu.fudan.se.sctap_lowcode_tool.service.SceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/scene")
public class SceneController {

    @Autowired
    private SceneService sceneService;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addScene(@RequestBody Map<String, String> request) {
        // 前端传来: { "sceneType": "F-city" }
        String sceneType = request.get("sceneType");

        // 执行业务
        Map<String, Object> result = sceneService.fetchAndParseGridData(sceneType);

        return ResponseEntity.ok(result);
    }
}