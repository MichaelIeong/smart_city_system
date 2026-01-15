package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeSummaryDTO;
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

    // ================== 新增：前端页面核心聚合接口 (Mock 替换) ==================

    /**
     * 1. 获取全局设备聚合信息
     * 用于前端：loadMeshData 时替换 mock 的 globalDeviceData
     * 请求示例：GET /api/devices/global-summary?sceneType=F-city
     */
    @GetMapping("/global-summary")
    public ResponseEntity<List<DeviceTypeSummaryDTO>> getGlobalSummary(@RequestParam String sceneType) {
        List<DeviceTypeSummaryDTO> list = tslDeviceService.getGlobalDeviceSummary(sceneType);
        return ResponseEntity.ok(list);
    }

    /**
     * 2. 获取网格内设备聚合信息
     * 用于前端：点击网格 fetchGridInfo 时替换 mock 的 deviceTypeData
     * 请求示例：GET /api/devices/grid-summary?gridId=f-city-1
     */
    @GetMapping("/grid-summary")
    public ResponseEntity<List<DeviceTypeSummaryDTO>> getGridSummary(@RequestParam String gridId) {
        List<DeviceTypeSummaryDTO> list = tslDeviceService.getGridDeviceSummary(gridId);
        return ResponseEntity.ok(list);
    }

    // ================== 原有接口 (保持不变或做少量适配) ==================

    /**
     * 根据设备类型(prodId)查询设备实例列表
     * GET /api/devices/instances?prodId=p_ai_camera_tst
     */
    @GetMapping("/instances")
    public ResponseEntity<?> getDeviceInstances(@RequestParam String prodId) {
        try {
            Map<String, Object> result = tslDeviceService.queryDeviceInstances(prodId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "服务异常：" + e.getMessage()));
        }
    }

    /**
     * 新增设备实例
     * POST /api/devices/instances
     */
    @PostMapping("/instances")
    public ResponseEntity<?> addDeviceInstance(@RequestBody Map<String, String> instanceData) {
        try {
            Map<String, Object> result = tslDeviceService.addDeviceInstance(instanceData);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "请求参数错误：" + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "新增设备实例失败：" + e.getMessage()));
        }
    }
}