package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeResponse;
import edu.fudan.se.sctap_lowcode_tool.service.DeviceTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deviceTypes")
@Tag(name = "DeviceTypeController", description = "设备状态控制器")
public class DeviceTypeController {

    @Autowired
    private DeviceTypeService deviceTypeService;

    @GetMapping("/{id}")
    public ResponseEntity<DeviceTypeResponse> getDeviceTypeById(@PathVariable int id) {
        System.out.println(deviceTypeService.getDeviceTypeById(id));
        return deviceTypeService.getDeviceTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<DeviceTypeResponse> getDeviceTypesByProjectId(
            @RequestParam(name = "project") int projectId) {
        return deviceTypeService.getDevicesByProjectId(projectId);
    }


}
