package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.DTO.PersonUpdateRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.SensorData;
import edu.fudan.se.sctap_lowcode_tool.model.*;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleBranchRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.OperatorRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class NodeRedService {

    @Autowired
    private FusionRuleRepository fusionRuleRepository;

    @Autowired
    private FusionRuleBranchRepository branchRepo;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private OperatorService operatorService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private PersonService personService;

    /**
     * 保存上传的规则（方案A：主干+分支）
     * 期望 msg 中至少包含：ruleJson、flowJson、fusionTarget
     * 可选：projectId、spaceId、branchName、status
     *
     * 示例 msg 字段：
     * - ruleJson: {...}，其中包含 rulename
     * - flowJson: {...}
     * - fusionTarget: "person"
     * - projectId: 123             // 可选
     * - spaceId: 456               // 可选，不传则创建“全局分支”
     * - branchName: "xxx 2"        // 可选，不传则用“主干名 + index”
     * - status: "active"|"inactive"// 可选，默认 inactive
     */
    @Transactional
    public void handleUploadRule(Map<String, JsonNode> msg) {
        JsonNode ruleJsonNode = msg.get("ruleJson");
        JsonNode flowJsonNode = msg.get("flowJson");
        JsonNode fusionTargetNode = msg.get("fusionTarget");

        if (ruleJsonNode == null || flowJsonNode == null || fusionTargetNode == null) {
            throw new IllegalArgumentException("缺少必要字段：ruleJson/flowJson/fusionTarget");
        }

        String ruleName = ruleJsonNode.path("rulename").asText(null);
        if (ruleName == null || ruleName.isBlank()) {
            throw new IllegalArgumentException("ruleJson.rulename 不能为空");
        }
        String fusionTarget = fusionTargetNode.asText();

        // 可选参数
        Integer projectId = msg.get("projectId") != null ? msg.get("projectId").asInt() : null;
        Integer spaceId   = msg.get("spaceId")   != null ? msg.get("spaceId").asInt()   : null;
        String  branchName= msg.get("branchName")!= null ? msg.get("branchName").asText(): null;
        String  status    = msg.get("status")    != null ? msg.get("status").asText()    : "inactive";

        // 1) 创建主干（仅 name / project）
        FusionRule rule = new FusionRule();
        rule.setRuleName(ruleName);
        if (projectId != null) {
            projectService.findById(projectId).ifPresent(rule::setProjectID);
        }
        // 主干不再存 fusionTarget/status/ruleJson/flowJson
        fusionRuleRepository.save(rule);

        // 2) 可选 space 关联
        SpaceInfo space = null;
        if (spaceId != null) {
            space = spaceRepository.findById(spaceId)
                    .orElseThrow(() -> new IllegalArgumentException("Space not found: " + spaceId));
        }

        // 3) 下一个分支序号
        int nextIdx = branchRepo.findMaxIndex(rule.getRuleId(), spaceId) + 1;
        String finalBranchName = (branchName == null || branchName.isBlank())
                ? ruleName + " " + nextIdx
                : branchName;

        // 4) 创建分支（承载可运行内容）
        FusionRuleBranch branch = new FusionRuleBranch();
        branch.setRule(rule);
        branch.setSpace(space);
        branch.setBranchIndex(nextIdx);
        branch.setBranchName(finalBranchName);
        branch.setFusionTarget(fusionTarget);
        branch.setStatus((status == null || status.isBlank()) ? "inactive" : status);
        branch.setRuleJson(ruleJsonNode.toString());
        branch.setFlowJson(flowJsonNode.toString());

        branchRepo.save(branch);

        System.out.println("已创建主干 ruleId=" + rule.getRuleId()
                + " 与分支 branchId=" + branch.getBranchId()
                + "（branchIndex=" + nextIdx + "）");
    }

    /**
     * 兼容旧接口：直接保存主干
     * 建议迁移到 handleUploadRule（主干+分支），此方法仅保留兼容性
     */
    @Deprecated
    public void addNewRule(FusionRule fusionRule) {
        // 旧逻辑：直接存主表。方案A下不再推荐这样做
        fusionRuleRepository.save(fusionRule);
    }

    /**
     * 根据项目 ID 获取传感器数据
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
     * 获取所有事件融合算子
     */
    public List<Operator> getAllOperators() {
        List<Operator> operators = new ArrayList<>();
        operators.addAll(operatorService.getAllUtilOperators());
        operators.addAll(operatorRepository.findAll());
        return operators;
    }

    /**
     * 获取与 Space 表有关联的所有表名（静态定义）
     */
    public List<String> getAllFusionTable() {
        return List.of("person");
    }

    /**
     * 根据表名修改数据
     */
    public void updateFusionTable(String fusionTarget, Object updateRequest) {
        // 默认示例：person 表
        if ("person".equals(fusionTarget) && updateRequest instanceof PersonUpdateRequest) {
            // 示例：固定 id=4 的人员更新
            personService.updatePerson(4, (PersonUpdateRequest) updateRequest);
        }
    }
}