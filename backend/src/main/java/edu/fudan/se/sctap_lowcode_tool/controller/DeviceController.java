package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "删除设备", description = "删除指定的设备。")
    public ResponseEntity<Void> deleteDevice(@PathVariable int deviceId) {
        return deviceService.deleteDevice(deviceId) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{deviceId}")
    @Operation(summary = "更新设备信息", description = "更新指定设备的详细信息。")
    public ResponseEntity<Void> updateDevice(@PathVariable int deviceId, @RequestBody DeviceInfo deviceInfo) {
        deviceInfo.setDeviceId(deviceId);
        deviceService.saveOrUpdateDevice(deviceInfo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{deviceId}/status")
    @Operation(summary = "查询设备状态", description = "获取指定设备的当前状态，如在线或离线。")
    public ResponseEntity<String> getDeviceStatus(@PathVariable int deviceId) {
        String status = deviceService.getDeviceStatus(deviceId);
        return status != null ? ResponseEntity.ok(status) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{deviceId}/url")
    @Operation(summary = "查询设备URL", description = "获取指定设备的URL。")
    public ResponseEntity<String> getDeviceURL(@PathVariable int deviceId) {
        String url = deviceService.getDeviceURL(deviceId);
        return url != null ? ResponseEntity.ok(url) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{deviceId}/data")
    @Operation(summary = "查询设备数据", description = "获取指定设备的数据。")
    public ResponseEntity<String> getDeviceData(@PathVariable int deviceId) {
        String data = deviceService.getDeviceData(deviceId);
        return data != null ? ResponseEntity.ok(data) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{deviceId}/isSensor")
    @Operation(summary = "查询设备是否为传感器", description = "查询当前设备是否为传感器。")
    public ResponseEntity<Boolean> getDeviceIsSensor(@PathVariable int deviceId) {
        Boolean isSensor = deviceService.getDeviceIsSensor(deviceId);
        return isSensor != null ? ResponseEntity.ok(isSensor) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{deviceId}/capabilities")
    @Operation(summary = "查询设备能力", description = "获取指定设备的功能和能力。")
    public ResponseEntity<String> getDeviceCapabilities(@PathVariable int deviceId) {
        String capabilities = deviceService.getDeviceCapabilities(deviceId);
        return capabilities != null ? ResponseEntity.ok(capabilities) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{deviceId}/type")
    @Operation(summary = "查询设备类型", description = "查询当前设备是否sensor/可操作设备。")
    public ResponseEntity<String> getDeviceType(@PathVariable int deviceId) {
        String type = deviceService.getDeviceType(deviceId);
        return type != null ? ResponseEntity.ok(type) : ResponseEntity.notFound().build();
    }


    @GetMapping("/{deviceId}")
    @Operation(summary = "获取设备信息", description = "根据设备Id获取设备的详细信息。")
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
