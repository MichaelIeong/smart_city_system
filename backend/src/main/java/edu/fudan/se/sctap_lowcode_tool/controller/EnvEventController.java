package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import edu.fudan.se.sctap_lowcode_tool.service.EnvEventService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 获取全部环境级事件列表
     * */
    @GetMapping("/all")
    public ResponseEntity<List<EnvEvent>> getAllEnvEventList() {
        return ResponseEntity.ok(envEventService.getAllEnvEventList());
    }

    /**
     * 分页查询环境级事件
     * */
    @GetMapping("/list")
    public PageDTO<EnvEvent> list(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String eventName,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return envEventService.list(eventType, eventName, pageNo, pageSize, sortField, sortOrder);
    }
}
