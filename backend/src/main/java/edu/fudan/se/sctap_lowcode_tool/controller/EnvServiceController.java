package edu.fudan.se.sctap_lowcode_tool.controller;


import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import edu.fudan.se.sctap_lowcode_tool.service.EnvServiceService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/envService")
public class EnvServiceController {
    @Resource
    private EnvServiceService envServiceService;

    /**
     * 根据网格Id获取环境级服务列表
     * */
    @GetMapping("/list/{gridId}")
    public ResponseEntity<List<EnvService>> getEnvServiceList(@PathVariable String gridId) {
        return ResponseEntity.ok(envServiceService.getEnvServiceList(gridId));
    }
}
