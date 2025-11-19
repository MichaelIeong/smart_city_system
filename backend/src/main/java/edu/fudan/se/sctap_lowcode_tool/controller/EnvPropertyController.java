package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.EnvProperty;
import edu.fudan.se.sctap_lowcode_tool.service.EnvPropertyService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/envProperty")
public class EnvPropertyController {

    @Resource
    private EnvPropertyService envPropertyService;

    /**
     * 根据网格Id获取环境级属性列表
     * */
    @GetMapping("/list/{gridId}")
    public ResponseEntity<List<EnvProperty>> getEnvPropertyList(@PathVariable String gridId) {
        return ResponseEntity.ok(envPropertyService.getEnvPropertyList(gridId));
    }
}
