package edu.fudan.se.sctap_lowcode_tool.controller;


import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceGroupDeployDetail;
import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceGroupSyncRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceGroupSyncResponse;
import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import edu.fudan.se.sctap_lowcode_tool.model.GridMesh;
import edu.fudan.se.sctap_lowcode_tool.service.EnvServiceService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 获取全部环境级服务列表
     * */
    @GetMapping("/all")
    public ResponseEntity<List<EnvService>> getAllEnvServiceList() {
        return ResponseEntity.ok(envServiceService.getAllEnvServiceList());
    }

    /**
     * 分页查询环境级服务
     * */
    @GetMapping("/list")
    public PageDTO<EnvService> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return envServiceService.list(name, description, pageNo, pageSize, sortField, sortOrder);
    }

    /**
     * 获取服务组部署详情
     * */
    @GetMapping("/deploy/detail/{id}")
    public ResponseEntity<List<ServiceGroupDeployDetail>> getServiceGroupDeployDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(envServiceService.getServiceGroupDeployDetail(id));
    }

    /**
     * 删除环境级服务
     * */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnvService(@PathVariable Integer id) {
        envServiceService.deleteEnvService(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据服务ID获取同类型的网格
     * */
    @GetMapping("/typeOfService/{serviceId}")
    public ResponseEntity<List<GridMesh>> getGridListByServiceId(@PathVariable Integer serviceId) {
        return ResponseEntity.ok(envServiceService.getGridListByServiceId(serviceId));
    }

    /**
     * 服务组同步下发
     * */
    @PostMapping("/sync")
    public ResponseEntity<List<ServiceGroupSyncResponse>> syncServiceGroup(@RequestBody ServiceGroupSyncRequest request) {
        return ResponseEntity.ok(envServiceService.syncServiceGroup(request));
    }
}
