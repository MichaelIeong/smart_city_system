package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "DeviceController", description = "设备状态控制器")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @PostMapping("/upload")
    @Operation(summary = "上传设备信息", description = "上传新的或更新现有设备的信息。")
    public ResponseEntity<DeviceInfo> postDevices(@RequestBody DeviceInfo deviceInfo) {
        return ResponseEntity.ok(deviceService.saveOrUpdateDevice(deviceInfo));
    }

    @DeleteMapping("/{deviceID}")
    @Operation(summary = "删除设备", description = "删除指定的设备。")
    public ResponseEntity<Void> deleteDevice(@PathVariable int deviceID) {
        return deviceService.deleteDevice(deviceID) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{deviceID}")
    @Operation(summary = "更新设备信息", description = "更新指定设备的详细信息。")
    public ResponseEntity<Void> updateDevice(@PathVariable int deviceID, @RequestBody DeviceInfo deviceInfo) {
        deviceInfo.setDeviceId(deviceID);
        deviceService.saveOrUpdateDevice(deviceInfo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{deviceID}/status")
    @Operation(summary = "查询设备状态", description = "获取指定设备的当前状态，如在线或离线。")
    public ResponseEntity<String> getDeviceStatus(@PathVariable int deviceID) {
        String status = deviceService.getDeviceStatus(deviceID);
        return status != null ? ResponseEntity.ok(status) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{deviceID}/url")
    @Operation(summary = "查询设备URL", description = "获取指定设备的URL。")
    public ResponseEntity<String> getDeviceURL(@PathVariable int deviceID) {
        String url = deviceService.getDeviceURL(deviceID);
        return url != null ? ResponseEntity.ok(url) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{deviceID}/data")
    @Operation(summary = "查询设备数据", description = "获取指定设备的数据。")
    public ResponseEntity<String> getDeviceData(@PathVariable int deviceID) {
        String data = deviceService.getDeviceData(deviceID);
        return data != null ? ResponseEntity.ok(data) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{deviceID}/capabilities")
    @Operation(summary = "查询设备能力", description = "获取指定设备的功能和能力。")
    public ResponseEntity<String> getDeviceCapabilities(@PathVariable int deviceID) {
        String capabilities = deviceService.getDeviceCapabilities(deviceID);
        return capabilities != null ? ResponseEntity.ok(capabilities) : ResponseEntity.notFound().build();
    }


    @GetMapping("/{deviceId}")
    @Operation(summary = "获取设备信息", description = "根据设备ID获取设备的详细信息。")
    public ResponseEntity<DeviceInfo> getDeviceById(@PathVariable int deviceId) {
        return deviceService.findById(deviceId)
                .map(ResponseEntity::ok)  // 如果找到了设备，返回200 OK和设备信息
                .orElseGet(() -> ResponseEntity.notFound().build());  // 如果没有找到设备，返回404 Not Found
    }

    @GetMapping("allDevices")
    @Operation(summary = "获取所有设备", description = "获取所有设备的详细信息。")
    public ResponseEntity<Iterable<DeviceInfo>> getAllDevices() {
        return ResponseEntity.ok(deviceService.findAll());
    }

    @PostMapping("/import")
    @Operation(summary = "导入设备信息", description = "从JSON文件导入设备信息。")
    public ResponseEntity<Void> importDevices(@RequestBody String json) {
        boolean isSuccess = deviceService.importDevices(json);
        return isSuccess ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @GetMapping("/export")
    @Operation(summary = "导出设备信息", description = "导出所有设备信息为JSON文件。")
    public ResponseEntity<String> exportDevices() {
        return deviceService.exportDevices()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(500).body("Error generating JSON"));
    }
}
