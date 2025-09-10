package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceCreateRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceResponse;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.DeviceNode;
import edu.fudan.se.sctap_lowcode_tool.service.DeviceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "DeviceController", description = "设备状态控制器")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    // Neo4j + MySQL 查询设备
    @GetMapping("/{deviceId}")
    public ResponseEntity<?> getDevice(@PathVariable String deviceId) {
        // 先尝试查 Neo4j 图结构（含完整关系）
        Optional<DeviceNode> nodeOpt = deviceService.findByDeviceId(deviceId);
        if (nodeOpt.isPresent()) {
            return ResponseEntity.ok(nodeOpt.get());
        }

        // fallback 到 MySQL（仅设备基本信息）
        Optional<DeviceResponse> sqlDevice = deviceService.findByDeviceIdFromMySQL(deviceId);
        return sqlDevice.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    // MySQL 查询：根据项目ID获取设备列表
    @GetMapping
    public ResponseEntity<Iterable<DeviceResponse>> getDevicesByProjectId(
            @RequestParam(name = "project") int projectId) {
        return ResponseEntity.ok(deviceService.findAllByProjectId(projectId));  // MySQL 查询
    }

    // 创建设备，同时保存到 MySQL 和 Neo4j
    @PostMapping
    public ResponseEntity<DeviceInfo> createDevice(@RequestBody DeviceInfo device) {
        DeviceInfo saved = deviceService.saveDevice(device); // MySQL & Neo4j
        return ResponseEntity.ok(saved);
    }

    // 更新设备，同时更新到 MySQL 和 Neo4j
    @PutMapping("/{deviceId}")
    public ResponseEntity<DeviceInfo> updateDevice(@PathVariable Integer deviceId, @RequestBody DeviceInfo device) {
        Optional<DeviceInfo> updated = deviceService.updateDevice(deviceId, device); // MySQL & Neo4j
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 删除设备，同时从 MySQL 和 Neo4j 中删除
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Integer deviceId) {
        deviceService.deleteDevice(deviceId);  // MySQL & Neo4j
        return ResponseEntity.noContent().build();
    }

}