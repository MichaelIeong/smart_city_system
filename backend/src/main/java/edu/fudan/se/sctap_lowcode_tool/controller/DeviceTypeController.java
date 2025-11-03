package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeResponse;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceTypeInfo;
import edu.fudan.se.sctap_lowcode_tool.service.DeviceTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deviceTypes")
@Tag(name = "DeviceTypeController", description = "设备类型控制器")
public class DeviceTypeController {

    @Autowired
    private DeviceTypeService deviceTypeService;

    // 正确注入 JdbcTemplate
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/{id}")
    public ResponseEntity<DeviceTypeResponse> getDeviceTypeById(@PathVariable int id) {
        return deviceTypeService.getDeviceTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(params = "project") // 保留原有逻辑，用于带 project 参数的情况
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

    // 新增接口：专门从 tsl_product 表中读取设备类型
    @GetMapping("/fromTslProduct")
    public List<Map<String, Object>> getAllDeviceTypesFromTslProduct() {
        String sql = "SELECT product_id AS deviceTypeId, " +
                "product_name AS deviceTypeName, " +
                "product_property AS deviceTypeAttributes, " +
                "product_function AS deviceTypeFunction " +
                "FROM tsl_product";
        return jdbcTemplate.queryForList(sql);
    }
}