package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceResponse;
import edu.fudan.se.sctap_lowcode_tool.service.DeviceService;
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

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDevice(@PathVariable int id) {
        return ResponseEntity.of(deviceService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Iterable<DeviceResponse>> getDevicesByProjectId(
            @RequestParam(name = "project") int projectId) {
        return ResponseEntity.ok(deviceService.findAllByProjectId(projectId));
    }


}
