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
@RequestMapping("/api/spaces")
@Tag(name = "SpaceController", description = "与空间操作相关的API接口")
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @PostMapping
    @Operation(summary = "创建新空间", description = "创建一个新的空间并返回它。")
    public ResponseEntity<SpaceInfo> createSpaceInfo(@RequestBody SpaceInfo spaceInfo) {
        return ResponseEntity.ok(spaceService.saveOrUpdateSpace(spaceInfo));
    }

    @DeleteMapping("/{spaceId}")
    @Operation(summary = "删除空间", description = "通过ID删除一个空间。")
    public ResponseEntity<Void> deleteSpaceInfo(@PathVariable int spaceId) {
        return spaceService.deleteSpace(spaceId) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{spaceId}")
    @Operation(summary = "更新空间信息", description = "更新一个空间的信息。")
    public ResponseEntity<SpaceInfo> updateSpaceInfo(@PathVariable int spaceId, @RequestBody SpaceInfo spaceInfo) {
        spaceInfo.setSpaceId(spaceId);
        spaceService.saveOrUpdateSpace(spaceInfo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/allSpaces")
    @Operation(summary = "获取所有空间")
    public ResponseEntity<Iterable<SpaceInfo>> findAllSpaces() {
        return ResponseEntity.ok(spaceService.findAllSpaces());
    }

    @GetMapping("/{spaceId}")
    @Operation(summary = "通过ID获取空间", description = "通过其ID检索空间。")
    public ResponseEntity<SpaceInfo> getSpaceInfoById(@PathVariable int spaceId) {
        return spaceService.findSpaceById(spaceId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/findId/{spaceName}")
    @Operation(summary = "通过空间名查找ID")
    public ResponseEntity<Integer> findIdBySpaceName(@PathVariable String spaceName) {
        int spaceId = spaceService.getSpaceIdByName(spaceName);
        return spaceId != -1 ? ResponseEntity.ok(spaceId) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{spaceId}/devices")
    @Operation(summary = "获取空间中的所有设备", description = "检索指定空间中所有的设备。")
    public ResponseEntity<Set<DeviceInfo>> getAllSpaceDevices(@PathVariable int spaceId) {
        return ResponseEntity.ok(spaceService.getAllSpaceDevices(spaceId));
    }
}