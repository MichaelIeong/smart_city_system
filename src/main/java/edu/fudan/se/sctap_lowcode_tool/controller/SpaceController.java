package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/spaces")
@Tag(name = "SpaceController", description = "与空间操作相关的API接口")
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @PostMapping
    @Operation(summary = "创建新空间", description = "创建一个新的空间并返回它。")
    public ResponseEntity<SpaceInfo> createSpaceInfo(@RequestBody SpaceInfo spaceInfo) {
        return ResponseEntity.ok(spaceService.saveSpaceInfo(spaceInfo));
    }

    @GetMapping("/{spaceId}")
    @Operation(summary = "通过ID获取空间", description = "通过其ID检索空间。")
    public ResponseEntity<SpaceInfo> getSpaceInfoById(@PathVariable int spaceId) {
        SpaceInfo spaceInfo = spaceService.getSpaceInfoById(spaceId);
        if (spaceInfo != null) {
            return ResponseEntity.ok(spaceInfo);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

//    @GetMapping("/{spaceId}/devices")
//    @Operation(summary = "获取空间中的所有设备", description = "检索指定空间中所有的设备。")
//    public ResponseEntity<Set<DeviceInfo>> getAllSpaceDevices(@PathVariable int spaceId) {
//        Set<DeviceInfo> devices = spaceService.getAllSpaceDevices(spaceId);
//        if (devices != null) {
//            return ResponseEntity.ok(devices);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }

    @PutMapping("/{spaceId}")
    @Operation(summary = "更新空间信息", description = "更新一个空间的信息。")
    public ResponseEntity<SpaceInfo> updateSpaceInfo(@PathVariable int spaceId, @RequestBody SpaceInfo spaceInfo) {
        SpaceInfo existingSpaceInfo = spaceService.getSpaceInfoById(spaceId);
        if (existingSpaceInfo != null) {
            spaceInfo.setSpaceId(spaceId);
            return ResponseEntity.ok(spaceService.updateSpaceInfo(spaceInfo));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{spaceId}")
    @Operation(summary = "删除空间", description = "通过ID删除一个空间。")
    public ResponseEntity<Void> deleteSpaceInfo(@PathVariable int spaceId) {
        SpaceInfo existingSpaceInfo = spaceService.getSpaceInfoById(spaceId);
        if (existingSpaceInfo != null) {
            spaceService.deleteSpaceInfoById(spaceId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}