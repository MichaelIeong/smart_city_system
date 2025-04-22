package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.DTO.SensorData;
import edu.fudan.se.sctap_lowcode_tool.model.*;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.OperatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NodeRedService {

    @Autowired
    private FusionRuleRepository fusionRuleRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private OperatorService operatorService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SpaceService spaceService;

    /**
     * 保存上传的规则
     *
     * @param msg 包含 ruleJson 和 flowJson 的 Map
     */
    public void handleUploadRule(Map<String, JsonNode> msg) {
        JsonNode ruleJson = msg.get("ruleJson");
        JsonNode flowJson = msg.get("flowJson");

        FusionRule fusionRule = new FusionRule();
        fusionRule.setFlowJson(flowJson.toString());
        fusionRule.setRuleJson(ruleJson.toString());
        fusionRule.setRuleName(ruleJson.get("rulename").asText());

        addNewRule(fusionRule);
    }

    /**
     * 根据项目 ID 获取传感器数据
     *
     * @param projectId 项目 ID
     * @return 包含传感器数据的 ResponseEntity
     */
    public ResponseEntity<?> getSensorDataByProjectId(int projectId) {
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
            if (devices == null || devices.isEmpty()) continue;

            for (DeviceInfo device : devices) {
                DeviceTypeInfo deviceType = device.getDeviceType();
                if (deviceType == null || !Boolean.TRUE.equals(deviceType.getIsSensor())) continue;

                SensorData sensorData = new SensorData();
                sensorData.setSensorId(device.getDeviceId());
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
     * 添加新的融合规则
     *
     * @param fusionRule 规则对象
     */
    public void addNewRule(FusionRule fusionRule) {
        fusionRuleRepository.save(fusionRule);
    }

    /**
     * 获取所有事件融合算子
     *
     * @return 所有 Operator 列表
     */
    public List<Operator> getAllOperators() {
        List<Operator> operators = new ArrayList<>();
        operators.addAll(operatorService.getAllUtilOperators());
        operators.addAll(operatorRepository.findAll());
        return operators;
    }

    /**
     * 获取与 Space 表有关联的所有表名（静态定义）
     *
     * @return 相关联表的表名列表
     */
    public List<String> getAllFusionTable() {
        return List.of(
                "person"            // 人员表
        );
    }
}