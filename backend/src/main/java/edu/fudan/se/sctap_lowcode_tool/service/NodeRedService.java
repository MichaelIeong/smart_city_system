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

    /*** 保存上传的规则（支持更新已有分支） */
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

        // 可选：projectId / spaceId / branchName / status
        Integer projectId = msg.get("projectId") != null ? msg.get("projectId").asInt() : null;
        Integer spaceId = msg.get("spaceId") != null ? msg.get("spaceId").asInt() : null;
        String branchNameReq = msg.get("branchName") != null ? msg.get("branchName").asText() : null;
        String statusReq = msg.get("status") != null ? msg.get("status").asText() : "inactive";

        // 关键：从 msg 里取 branchId（Node-RED publish.js 里要发上来）
        Integer branchId = null;
        JsonNode branchIdNode = msg.get("branchId");
        if (branchIdNode != null && branchIdNode.isInt()) {
            branchId = branchIdNode.asInt();
        } else if (branchIdNode != null && branchIdNode.isTextual()) {
            try {
                branchId = Integer.parseInt(branchIdNode.asText());
            } catch (NumberFormatException ignore) {
            }
        }

        // ===== 1) 若带 branchId，则更新已有分支 =====
        if (branchId != null) {
            FusionRuleBranch branch = branchRepo.findById(branchId).orElseThrow(() -> new IllegalArgumentException("Branch not found"));

            // 可选：更新主干名称
            FusionRule rule = branch.getRule();
            if (rule != null && ruleName != null && !ruleName.isBlank()) {
                rule.setRuleName(ruleName);
                fusionRuleRepository.save(rule);
            }

            // 可选：更新 space（一般编辑不改 space，可按需保留）
            if (spaceId != null) {
                SpaceInfo space = spaceRepository.findById(spaceId).orElseThrow(() -> new IllegalArgumentException("Space not found: " + spaceId));
                branch.setSpace(space);
            }

            // 编辑已有分支时：优先尊重显式传入的 branchName，不动的话保持原名
            if (branchNameReq != null && !branchNameReq.isBlank()) {
                branch.setBranchName(branchNameReq.trim());
            }
            if (statusReq != null && !statusReq.isBlank()) {
                branch.setStatus(statusReq.trim());
            }

            branch.setFusionTarget(fusionTarget);
            branch.setRuleJson(ruleJsonNode.toString());
            branch.setFlowJson(flowJsonNode.toString());

            branchRepo.save(branch);

            System.out.println("已更新分支 branchId=" + branchId + "（ruleId=" + (rule != null ? rule.getRuleId() : null) + "）");
            return;
        }

        // ===== 2) 否则走“新建主干+分支”逻辑 =====

        // 先建主干
        FusionRule rule = new FusionRule();
        rule.setRuleName(ruleName);
        if (projectId != null) {
            projectService.findById(projectId).ifPresent(rule::setProjectID);
        }
        fusionRuleRepository.save(rule);

        // 取空间（可为空）
        SpaceInfo space = null;
        if (spaceId != null) {
            space = spaceRepository.findById(spaceId).orElseThrow(() -> new IllegalArgumentException("Space not found: " + spaceId));
        }

        // ★ 根据 ruleJson 里 Sensor 的 location 自动生成实例名
        String autoNameByLocation = buildBranchNameFromRuleJson(ruleJsonNode);

        // 分支名策略：
        // 1) 前端显式传入 branchName
        // 2) 若能从 ruleJson 里解析出 location，则用 "loc1+loc2" 这种形式
        // 3) 再其次用空间名
        // 4) 否则回退 ruleName
        String finalBranchName;
        if (branchNameReq != null && !branchNameReq.isBlank()) {
            finalBranchName = branchNameReq.trim();
        } else if (autoNameByLocation != null && !autoNameByLocation.isBlank()) {
            finalBranchName = autoNameByLocation;
        } else if (space != null && space.getSpaceName() != null && !space.getSpaceName().isBlank()) {
            finalBranchName = space.getSpaceName().trim();
        } else {
            finalBranchName = ruleName;
        }

        // 创建分支（承载可运行内容）
        FusionRuleBranch branch = new FusionRuleBranch();
        branch.setRule(rule);
        branch.setSpace(space);
        branch.setBranchName(finalBranchName);
        branch.setFusionTarget(fusionTarget);
        branch.setStatus((statusReq == null || statusReq.isBlank()) ? "inactive" : statusReq.trim());
        branch.setRuleJson(ruleJsonNode.toString());
        branch.setFlowJson(flowJsonNode.toString());

        branchRepo.save(branch);

        System.out.println("已创建主干 ruleId=" + rule.getRuleId() + " 与分支 branchId=" + branch.getBranchId() + "，branchName=" + finalBranchName);
    }


    /**
     * 从 ruleJson 中提取所有 Sensor 节点的 location，
     * 用 "A+B+..." 的形式拼成分支名；若没有则返回 null。
     */
    private String buildBranchNameFromRuleJson(JsonNode ruleJsonNode) {
        if (ruleJsonNode == null || !ruleJsonNode.isObject()) return null;

        // LinkedHashSet 保持去重 + 顺序
        Set<String> locations = new LinkedHashSet<>();

        ruleJsonNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            if ("steps".equals(key) || "rulename".equals(key)) return;

            JsonNode node = entry.getValue();
            if (node != null && node.isObject()) {
                String type = node.path("type").asText("");
                if ("Sensor".equalsIgnoreCase(type)) {
                    String loc = node.path("location").asText(null);
                    if (loc != null) {
                        loc = loc.trim();
                        if (!loc.isEmpty()) {
                            locations.add(loc);
                        }
                    }
                }
            }
        });

        if (locations.isEmpty()) {
            return null;
        }
        // 一个或多个 location，用 '+' 拼起来
        return String.join("+", locations);
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
        return List.of("person", "null");
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