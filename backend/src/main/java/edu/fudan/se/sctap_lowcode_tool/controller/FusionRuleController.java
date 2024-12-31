package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.DTO.SensorData;
import edu.fudan.se.sctap_lowcode_tool.model.*;
import edu.fudan.se.sctap_lowcode_tool.service.FusionRuleService;
import edu.fudan.se.sctap_lowcode_tool.service.ProjectService;
import edu.fudan.se.sctap_lowcode_tool.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/api/fusion")
@Tag(name = "FusionController", description = "事件融合控制器")
public class FusionRuleController {

    @Autowired
    private FusionRuleService fusionRuleService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private ProjectService projectService;

    /**
     * 上传新的规则，并立即执行 Node-RED JSON 的处理逻辑，同时保存规则到数据库。
     *
     * @param msg 包含 ruleJson 和 flowJson 的 Map
     * @return 操作结果
     */
    @Operation(summary = "上传并处理规则", description = "用户在 Node-RED 构建好规则，传给后端，加入到数据库并立即执行")
    @PostMapping("/uploadrule")
    public ResponseEntity<Void> saveAndProcessRule(@RequestBody Map<String, JsonNode> msg) {
        JsonNode ruleJson = msg.get("ruleJson");
        JsonNode flowJson = msg.get("flowJson");

        // 创建规则对象并保存到数据库
        FusionRule fusionRule = new FusionRule();
        fusionRule.setFlowJson(flowJson.toString());
        fusionRule.setRuleJson(ruleJson.toString());
        fusionRule.setRuleName(ruleJson.get("rulename").asText());
        fusionRuleService.addNewRule(fusionRule);

        // 调用服务处理 Node-RED JSON
        fusionRuleService.processNodeRedJson(ruleJson);

        return ResponseEntity.ok().build();
    }

    /**
     * 获取规则列表。
     *
     * @param request Http 请求对象
     * @return 规则列表
     */
    @Operation(summary = "获取规则列表", description = "将规则列表传给前端")
    @GetMapping("/getRuleList")
    public ResponseEntity<?> getRuleList(HttpServletRequest request) {
        List<FusionRule> fusionRuleList = fusionRuleService.getRuleList();
        return ResponseEntity.ok(fusionRuleList);
    }

    /**
     * 根据项目 ID 获取 Sensor 节点数据。
     *
     * @param projectId 项目 ID
     * @return Sensor 数据列表
     */
    @Operation(summary = "获取 Sensor 节点数据", description = "联合查询传给前端")
    @GetMapping("/sensor/{projectId}")
    public ResponseEntity<?> getSensorData(@PathVariable int projectId) {
        // 查找项目是否存在
        Optional<ProjectInfo> projectInfo = projectService.findById(projectId);
        if (projectInfo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到对应 ID 的项目！");
        }

        // 查找项目中的空间信息
        List<SpaceInfo> spaceInfoList = spaceService.findSpacesByProjectId(projectId);
        if (spaceInfoList.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // 存储所有的 SensorData DTO
        List<SensorData> sensorDataList = new ArrayList<>();

        // 遍历空间信息，查找与设备相关的传感器数据
        for (SpaceInfo spaceInfo : spaceInfoList) {
            Set<DeviceInfo> devices = spaceInfo.getSpaceDevices();
            if (devices == null || devices.isEmpty()) {
                continue;
            }

            // 遍历每个设备并将其转换为 SensorData
            for (DeviceInfo device : devices) {
                DeviceTypeInfo deviceType = device.getDeviceType();
                // 只处理设备类型是传感器的设备
                if (deviceType == null || !Boolean.TRUE.equals(deviceType.getIsSensor())) {
                    continue;
                }

                // 创建并填充 SensorData DTO
                SensorData sensorData = new SensorData();
                sensorData.setSensorId(device.getDeviceId()); // 使用设备ID作为 sensorId
                sensorData.setDeviceName(device.getDeviceName());
                sensorData.setDeviceType(deviceType.getDeviceTypeName());
                sensorData.setLocation(spaceInfo.getSpaceName());

                // 获取设备的功能列表
                List<String> functions = new ArrayList<>();
                Set<ActuatingFunctionDevice> actuatingFunctions = device.getActuatingFunctions();
                if (actuatingFunctions != null && !actuatingFunctions.isEmpty()) {
                    for (ActuatingFunctionDevice functionDevice : actuatingFunctions) {
                        functions.add(functionDevice.getActuatingFunction().getName());
                    }
                }
                sensorData.setFunction(functions); // 设置功能列表

                // 将 SensorData 添加到结果列表中
                sensorDataList.add(sensorData);
            }
        }

        // 返回 SensorData DTO 列表
        return ResponseEntity.ok(sensorDataList);
    }

    /**
     * 获取所有事件融合算子。
     *
     * @return 算子列表
     */
    @Operation(summary = "获取所有事件融合算子", description = "获取所有事件融合算子")
    @GetMapping("/operator/")
    public List<Operator> getAllOperators() {
        return fusionRuleService.getAllOperators();
    }
}