package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeWithFunctionsDTO;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceTypeInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.DeviceTypeNode;
import edu.fudan.se.sctap_lowcode_tool.service.DeviceTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deviceTypes")
@Tag(name = "DeviceTypeController", description = "设备类型控制器")
public class DeviceTypeController {

    @Autowired
    private DeviceTypeService deviceTypeService;

    @GetMapping("/{id}")
    public ResponseEntity<DeviceTypeResponse> getDeviceTypeById(@PathVariable int id) {
        return deviceTypeService.getDeviceTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<DeviceTypeResponse> getDeviceTypesByProjectId(@RequestParam(name = "project") int projectId) {
        return deviceTypeService.getDevicesByProjectId(projectId);
    }

    @PostMapping
    public ResponseEntity<DeviceTypeInfo> createDeviceType(@RequestBody DeviceTypeInfo deviceType) {
        return ResponseEntity.ok(deviceTypeService.saveDeviceType(deviceType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceTypeInfo> updateDeviceType(@PathVariable int id,
                                                           @RequestBody DeviceTypeInfo deviceType) {
        return deviceTypeService.updateDeviceType(id, deviceType)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeviceType(@PathVariable int id) {
        deviceTypeService.deleteDeviceType(id);
        return ResponseEntity.noContent().build();
    }

    //neo4j
    // 新增一个设备类型
//    @PostMapping("/deviceTypes")
//    public ResponseEntity<DeviceTypeNode> createDeviceType(@RequestBody DeviceTypeNode dto) {
//        DeviceTypeNode saved = deviceTypeService.createDeviceType(dto);
//        return ResponseEntity.ok(saved);
//    }
//    // 给某个space新增设备类型
//    @PostMapping("/spaces/{spaceId}/deviceTypes/{deviceTypeId}")
//    public ResponseEntity<Void> addDeviceTypeToSpace(
//            @PathVariable Integer spaceId,
//            @PathVariable Integer deviceTypeId) {
//        deviceTypeService.addDeviceTypeToSpace(deviceTypeId, spaceId);
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/byspace")
//    public ResponseEntity<List<DeviceTypeWithFunctionsDTO>> getDeviceTypesBySpace(
//            @RequestParam("spaceId") Integer spaceId) {
//        List<DeviceTypeWithFunctionsDTO> deviceTypes = deviceTypeService.listDeviceTypesAndFunctionsBySpace(spaceId);
//        if (deviceTypes.isEmpty()) {
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.ok(deviceTypes);
//    }

    @GetMapping("/alldevicetype")
    public ResponseEntity<List<DeviceTypeResponse>> getAllDeviceTypesByProjectId(
            @RequestParam("projectId") int projectId) {
        List<DeviceTypeResponse> list = deviceTypeService.getDevicesByProjectId(projectId);
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }
}