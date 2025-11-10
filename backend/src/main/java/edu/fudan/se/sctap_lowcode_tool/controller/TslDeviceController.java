package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.service.TslDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class TslDeviceController {

    @Autowired
    private TslDeviceService tslDeviceService;

    /**
     * 根据设备类型(prodId)查询设备实例列表
     * 示例：
     *   GET /api/devices/instances?prodId=p_ai_camera_tst
     */
    @GetMapping("/instances")
    public ResponseEntity<?> getDeviceInstances(@RequestParam String prodId) {
        try {
            Map<String, Object> result = tslDeviceService.queryDeviceInstances(prodId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}