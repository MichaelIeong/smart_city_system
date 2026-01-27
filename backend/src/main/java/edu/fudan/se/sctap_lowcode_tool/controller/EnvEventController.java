package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.EventFusionDeployDetail;
import edu.fudan.se.sctap_lowcode_tool.DTO.EventFusionSyncRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.EventFusionSyncResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import edu.fudan.se.sctap_lowcode_tool.model.GridMesh;
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
            @RequestParam(required = false) Integer projectId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return envEventService.list(eventType, eventName, projectId, pageNo, pageSize, sortField, sortOrder);
    }

    /**
     * 获取事件融合部署详情
     * */
    @GetMapping("/deploy/detail/{id}")
    public ResponseEntity<List<EventFusionDeployDetail>> getEventFusionDeployDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(envEventService.getEventFusionDeployDetail(id));
    }

    /**
     * 删除环境级事件
     * */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnvEvent(@PathVariable Integer id) {
        envEventService.deleteEnvEvent(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据事件ID获取同类型的网格
     * */
    @GetMapping("/typeOfEvent/{eventId}")
    public ResponseEntity<List<GridMesh>> getGridListByEventId(@PathVariable Integer eventId) {
        return ResponseEntity.ok(envEventService.getGridListByEventId(eventId));
    }

    /**
     * 事件融合同步下发
     * */
    @PostMapping("/sync")
    public ResponseEntity<List<EventFusionSyncResponse>> syncEventFusion(@RequestBody EventFusionSyncRequest request) {
        return ResponseEntity.ok(envEventService.syncEventFusion(request));
    }
}
