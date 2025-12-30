package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import edu.fudan.se.sctap_lowcode_tool.service.EnvEventService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/envEvent")
public class EnvEventController {

    @Resource
    private EnvEventService envEventService;

    /**
     * 根据网格Id获取环境级事件列表
     * */
    @GetMapping("/list/{gridId}")
    public ResponseEntity<List<EnvEvent>> getEnvEventList(@PathVariable String gridId) {
        return ResponseEntity.ok(envEventService.getEnvEventList(gridId));
    }
}
