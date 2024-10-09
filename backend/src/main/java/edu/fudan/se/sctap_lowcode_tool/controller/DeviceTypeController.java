package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceTypeInfo;
import edu.fudan.se.sctap_lowcode_tool.service.DeviceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device-types")
@Tag(name = "DeviceTypeController", description = "设备状态控制器")
public class DeviceTypeController {

    @Autowired
    private DeviceTypeService deviceTypeService;

    @PostMapping("/upload")
    @Operation(summary = "上传设备类型", description = "上传新的或更新现有的设备类型。")
    public ResponseEntity<DeviceTypeInfo> postDevicesType(@RequestBody DeviceTypeInfo deviceTypeInfo) {
        return ResponseEntity.ok(deviceTypeService.saveOrUpdateDeviceType(deviceTypeInfo));
    }

    @DeleteMapping("/{typeId}")
    @Operation(summary = "删除设备类型", description = "删除指定的设备类型。")
    public ResponseEntity<Void> deleteDeviceType(@PathVariable int typeId) {
        return deviceTypeService.deleteType(typeId) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }


    @GetMapping("/{typeId}/isSensor")
    @Operation(summary = "查询类型是否为传感器", description = "查询当前设备类型是否为传感器。")
    public ResponseEntity<Boolean> getTypeIsSensor(@PathVariable int typeId) {
        Boolean isSensor = deviceTypeService.getTypeIsSensor(typeId);
        return isSensor != null ? ResponseEntity.ok(isSensor) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{typeId}")
    @Operation(summary = "获取设备类型信息", description = "根据类型ID获取类型的详细信息。")
    public ResponseEntity<DeviceTypeInfo> getDeviceTypeById(@PathVariable int typeId) {
        return deviceTypeService.findById(typeId)
                .map(ResponseEntity::ok)  // 如果找到了设备，返回200 OK和设备信息
                .orElseGet(() -> ResponseEntity.notFound().build());  // 如果没有找到设备，返回404 Not Found
    }

    @GetMapping("allTypes")
    @Operation(summary = "获取所有设备类型", description = "获取所有设备类型的详细信息。")
    public ResponseEntity<Iterable<DeviceTypeInfo>> getAllDeviceTypes() {
        return ResponseEntity.ok(deviceTypeService.findAll());
    }
}
