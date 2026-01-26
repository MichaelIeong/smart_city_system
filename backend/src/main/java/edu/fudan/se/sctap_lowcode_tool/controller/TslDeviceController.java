package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeSummaryDTO;
import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import edu.fudan.se.sctap_lowcode_tool.model.TslDevice;
import edu.fudan.se.sctap_lowcode_tool.service.AppRuleService;
import edu.fudan.se.sctap_lowcode_tool.service.EnvEventService;
import edu.fudan.se.sctap_lowcode_tool.service.EnvServiceService;
import edu.fudan.se.sctap_lowcode_tool.service.TslDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class TslDeviceController {

    @Autowired
    private TslDeviceService tslDeviceService;

    @Autowired
    private EnvEventService envEventService;
    @Autowired
    private EnvServiceService envServiceService;
    @Autowired
    private AppRuleService appRuleService;

    /**
     * 获取设备列表
     * 对应前端: fetchDeviceData (axios.get('/api/devices', ...))
     * 支持 mesh_nature (场景) 过滤
     */
    @GetMapping
    public ResponseEntity<List<TslDevice>> getDevices(
            @RequestParam(value = "project", required = false) Long projectId,
            @RequestParam(value = "mesh_nature", required = false) String meshNature) {

        List<TslDevice> list = tslDeviceService.getDevicesByProject(projectId, meshNature);
        return ResponseEntity.ok(list);
    }

    /**
     * 根据设备类型(prodId)查询设备实例列表
     * 对应前端: fetchDeviceInstancesByType
     * 增加 mesh_nature 参数
     */
    @GetMapping("/instances")
    public ResponseEntity<?> getDeviceInstances(
            @RequestParam String prodId,
            @RequestParam(value = "mesh_nature", required = false) String meshNature) { // 新增参数
        try {
            // 将参数传递给 Service
            Map<String, Object> result = tslDeviceService.queryDeviceInstances(prodId, meshNature);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "服务异常：" + e.getMessage()));
        }
    }


    /**
     * 1. 获取全局设备聚合信息
     */
    @GetMapping("/global-summary")
    public ResponseEntity<List<DeviceTypeSummaryDTO>> getGlobalSummary(@RequestParam String sceneType) {
        List<DeviceTypeSummaryDTO> list = tslDeviceService.getGlobalDeviceSummary(sceneType);
        return ResponseEntity.ok(list);
    }

    /**
     * 2. 获取网格内设备聚合信息
     */
    @GetMapping("/grid-summary")
    public ResponseEntity<List<DeviceTypeSummaryDTO>> getGridSummary(@RequestParam String gridId) {
        List<DeviceTypeSummaryDTO> list = tslDeviceService.getGridDeviceSummary(gridId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/global-events")
    public ResponseEntity<List<EnvEvent>> getGlobalEvents() {
        return ResponseEntity.ok(envEventService.getEnvEventList("crossRegion"));
    }

    @GetMapping("/global-services")
    public ResponseEntity<List<EnvService>> getGlobalServices() {
        return ResponseEntity.ok(envServiceService.getEnvServiceList("crossRegion"));
    }

    @GetMapping("/global-applications")
    public ResponseEntity<List<AppRuleInfo>> getGlobalApplications() {
        return ResponseEntity.ok(appRuleService.getAppRulesByGridId("crossRegion"));
    }


    // 新增接口
    @PostMapping("/instances")
    public ResponseEntity<?> addDeviceInstance(@RequestBody Map<String, Object> params) {
        try {
            return ResponseEntity.ok(tslDeviceService.addDeviceInstance(params));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/batch")
    public ResponseEntity<?> deleteDeviceInstances(@RequestBody List<String> deviceIds) {
        try {
            tslDeviceService.deleteDeviceInstances(deviceIds);
            return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "删除失败: " + e.getMessage()));
        }
    }
}