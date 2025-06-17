package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceResponse;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.service.DeviceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/device")
@Tag(name = "DeviceController", description = "设备状态控制器")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable int id) {
        return ResponseEntity.of(deviceService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Iterable<DeviceResponse>> getDevicesByProjectId(
            @RequestParam(name = "project") int projectId) {
        return ResponseEntity.ok(deviceService.findAllByProjectId(projectId));
    }

    @PostMapping
    public ResponseEntity<DeviceInfo> createDevice(@RequestBody DeviceInfo device) {
        DeviceInfo saved = deviceService.saveDevice(device);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceInfo> updateDevice(@PathVariable Integer id, @RequestBody DeviceInfo device) {
        Optional<DeviceInfo> updated = deviceService.updateDevice(id, device);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Integer id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}