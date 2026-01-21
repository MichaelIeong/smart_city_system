package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeResponse;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceTypeInfo;
import edu.fudan.se.sctap_lowcode_tool.model.TslProduct; // 1. 引入实体
import edu.fudan.se.sctap_lowcode_tool.service.DeviceTypeService;
import edu.fudan.se.sctap_lowcode_tool.service.TslDeviceService; // 2. 引入新写的Service
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/deviceTypes")
@Tag(name = "DeviceTypeController", description = "设备类型控制器")
public class DeviceTypeController {

    @Autowired
    private DeviceTypeService deviceTypeService;

    @Autowired
    private TslDeviceService tslDeviceService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/{id}")
    public ResponseEntity<DeviceTypeResponse> getDeviceTypeById(@PathVariable int id) {
        return deviceTypeService.getDeviceTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(params = "project")
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

    /**
     * 获取设备类型列表，支持场景过滤
     * 请求 URL: /api/deviceTypes/fromTslProduct?mesh_nature=F-city
     */
    @GetMapping("/fromTslProduct")
    public List<Map<String, Object>> getAllDeviceTypesFromTslProduct(
            @RequestParam(value = "mesh_nature", required = false) String meshNature // 新增参数
    ) {
        List<TslProduct> products = tslDeviceService.getProductTypesByScene(meshNature);

        // 将 Entity 转换为前端需要的 Map 结构
        return products.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("deviceTypeId", p.getProductId());
            map.put("deviceTypeName", p.getProductName());

            // 注意：假设 TslProduct 实体类中有这些字段的 getter
            // 如果实体类里没有 mapped 到 product_property，这行可能取不到值，请检查 TslProduct.java
            map.put("deviceTypeAttributes", p.getProductProperty());
            map.put("deviceTypeFunction", p.getProductFunction());

            return map;
        }).collect(Collectors.toList());

    }

    @PostMapping("/add")
    public ResponseEntity<?> addDeviceTypeFromMap(@RequestBody Map<String, String> deviceTypeData) {
        try {
            Map<String, Object> result = deviceTypeService.addDeviceType(deviceTypeData);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "请求参数错误：" + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "新增设备类型失败：" + e.getMessage()));
        }
    }
}