package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.DTO.SensorData;
import edu.fudan.se.sctap_lowcode_tool.model.*;
import edu.fudan.se.sctap_lowcode_tool.service.AuthenticationService;
import edu.fudan.se.sctap_lowcode_tool.service.FusionRuleService;
import edu.fudan.se.sctap_lowcode_tool.service.ProjectService;
import edu.fudan.se.sctap_lowcode_tool.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Operation(summary = "上传新的规则", description = "用户在node-red构建好规则，传给后端，加入到数据库")
    @PostMapping("/uploadrule")
    public ResponseEntity<Void> saveRule(@RequestBody Map<String, JsonNode> msg) {
        System.out.println("上传新规则/更新规则");
        JsonNode ruleJson = msg.get("ruleJson");
        JsonNode flowJson = msg.get("flowJson");
        System.out.println(flowJson);
        FusionRule fusionRule = new FusionRule();
        fusionRule.setFlowJson(flowJson.toString());
        fusionRule.setRuleJson(ruleJson.toString());
        fusionRule.setRuleName(ruleJson.get("rulename").toString());
        System.out.println(flowJson);
        System.out.println(ruleJson);
        System.out.println(ruleJson.get("rulename").toPrettyString());
        //如果name一样就更新
        fusionRuleService.addNewRule(fusionRule);
        return ResponseEntity.ok().build();

    }

    @Operation(summary = "获取规则列表", description = "将规则list传给前端")
    @GetMapping("/getRuleList")
    public ResponseEntity<?> getRuleList(HttpServletRequest request) {
        System.out.println("获取规则列表");
        // 验证token存在及有效性

        List<FusionRule> fusionRuleList = new ArrayList<>();
        fusionRuleList = fusionRuleService.getRuleList();
        System.out.println(fusionRuleList);
        return ResponseEntity.ok(fusionRuleList);

    }

    @Operation(summary = "获取sensor节点数据", description = "联合查询传给前端")
    @GetMapping("/sensor/{projectId}")
    public ResponseEntity<?> getSensorData(@PathVariable int projectId) {
        //location space表/device/type/function
        //现根据id找到空间列表
        Optional<ProjectInfo> projectInfo = projectService.findById(1);
        System.out.println(projectInfo);
        List<SpaceInfo> spaceInfoList = spaceService.findSpacesByProjectId(1);
        System.out.println(spaceInfoList);
        List<SensorData> sensorDataList = new ArrayList<>();
        for(SpaceInfo spaceInfo : spaceInfoList){
            //获取该空间中的device/deviceType
            Set<DeviceInfo> devices = spaceInfo.getSpaceDevices();
            // 遍历 Set<DeviceInfo>
            for (DeviceInfo device : devices) {
                DeviceTypeInfo deviceType = device.getDeviceType();

                // 检查 deviceType 是否为传感器
                if (deviceType != null && Boolean.TRUE.equals(deviceType.getIsSensor())) {
                    // 创建 SensorData 对象
                    SensorData sensorData = new SensorData();
                    sensorData.setDeviceName(device.getDeviceName());  // 设置设备名称
                    sensorData.setDeviceType(deviceType.getDeviceTypeName());  // 设置设备类型
                    sensorData.setLocation(spaceInfo.getSpaceName());  // 将 location 设置为 "room"

                    // 如果需要将设备的执行功能作为 function 列表
                    List<String> functions = new ArrayList<>();
                    device.getActuatingFunctions().forEach(functionDevice -> {
                        System.out.println(functionDevice.getActuatingFunction().getName());
                        functions.add(functionDevice.getActuatingFunction().getName().toString());  // 假设 functionDevice 有合适的 toString 方法
                    });
                    sensorData.setFunction(functions);  // 设置 function 列表

                    // 将 SensorData 添加到列表中
                    sensorDataList.add(sensorData);
                }
            }
        }
        return ResponseEntity.ok(sensorDataList);
    }

//    @Operation(summary = "获取operator节点数据", description = "联合查询传给前端")
//    @GetMapping("/operator")
//    public ResponseEntity<?> getOperatorData(@PathVariable int projectId) {
//        //location space表/device/type/function
//        //现根据peojectid找到projectinfo
//        Optional<ProjectInfo> projectInfo = projectService.findById(projectId);
//        //List<SpaceInfo> spaceInfoList = spaceService.findByProjectInfo(projectInfo.get());
//        List<SpaceInfo> spaceInfoList = spaceService.findSpacesByProjectId(projectId);
//        List<SensorData> sensorDataList = new ArrayList<>();
//        for(SpaceInfo spaceInfo : spaceInfoList){
//            //获取该空间中的device/deviceType
//            Set<DeviceInfo> devices = spaceInfo.getSpaceDevices();
//            // 遍历 Set<DeviceInfo>
//            for (DeviceInfo device : devices) {
//                DeviceTypeInfo deviceType = device.getDeviceType();
//
//                // 检查 deviceType 是否为传感器
//                if (deviceType != null && Boolean.TRUE.equals(deviceType.getIsSensor())) {
//                    // 创建 SensorData 对象
//                    SensorData sensorData = new SensorData();
//                    sensorData.setDeviceName(device.getDeviceName());  // 设置设备名称
//                    sensorData.setDeviceType(deviceType.getDeviceTypeName());  // 设置设备类型
//                    sensorData.setLocation(spaceInfo.getSpaceName());  // 将 location 设置为 "room"
//
//                    // 如果需要将设备的执行功能作为 function 列表
//                    List<String> functions = new ArrayList<>();
//                    device.getActuatingFunctions().forEach(functionDevice -> {
//                        System.out.println(functionDevice);
//                        functions.add(functionDevice.toString());  // 假设 functionDevice 有合适的 toString 方法
//                    });
//                    sensorData.setFunction(functions);  // 设置 function 列表
//
//                    // 将 SensorData 添加到列表中
//                    sensorDataList.add(sensorData);
//                }
//            }
//        }
//        return ResponseEntity.ok(sensorDataList);
//    }
}
