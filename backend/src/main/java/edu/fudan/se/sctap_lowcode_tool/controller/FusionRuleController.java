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
        Optional<ProjectInfo> projectInfo = projectService.findById(projectId);
        if (projectInfo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到对应 ID 的项目！");
        }

        List<SpaceInfo> spaceInfoList = spaceService.findSpacesByProjectId(projectId);
        if (spaceInfoList.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<SensorData> sensorDataList = new ArrayList<>();
        for (SpaceInfo spaceInfo : spaceInfoList) {
            Set<DeviceInfo> devices = spaceInfo.getSpaceDevices();
            if (devices == null || devices.isEmpty()) {
                continue;
            }

            for (DeviceInfo device : devices) {
                DeviceTypeInfo deviceType = device.getDeviceType();
                if (deviceType == null || !Boolean.TRUE.equals(deviceType.getIsSensor())) {
                    continue;
                }

                SensorData sensorData = new SensorData();
                sensorData.setDeviceName(device.getDeviceName());
                sensorData.setDeviceType(deviceType.getDeviceTypeName());
                sensorData.setLocation(spaceInfo.getSpaceName());

                List<String> functions = new ArrayList<>();
                Set<ActuatingFunctionDevice> actuatingFunctions = device.getActuatingFunctions();
                if (actuatingFunctions != null && !actuatingFunctions.isEmpty()) {
                    for (ActuatingFunctionDevice functionDevice : actuatingFunctions) {
                        functions.add(functionDevice.getActuatingFunction().getName());
                    }
                }
                sensorData.setFunction(functions);
                sensorDataList.add(sensorData);
            }
        }
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