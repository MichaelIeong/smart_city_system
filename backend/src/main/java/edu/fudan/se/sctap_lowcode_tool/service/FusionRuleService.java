package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.model.*;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.OperatorRepository; // 新增
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FusionRuleService {

    @Autowired
    private FusionRuleRepository fusionRuleRepository;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private OperatorRepository operatorRepository; // 新增的注入

    /**
     * 获取所有Operator记录
     */
    public List<Operator> getAllOperators() {
        return operatorRepository.findAll();
    }

    /**
     * 添加或更新规则。
     *
     * @param fusionRule 要添加或更新的规则对象
     */
    public void addNewRule(FusionRule fusionRule) {
        FusionRule existRule = fusionRuleRepository.findByRuleName(fusionRule.getRuleName());
        if (existRule != null) {
            fusionRule.setRuleId(existRule.getRuleId());
        }
        fusionRuleRepository.save(fusionRule);
    }

    /**
     * 获取所有规则的列表。
     *
     * @return 数据库中所有规则的列表
     */
    public List<FusionRule> getRuleList() {
        return fusionRuleRepository.findAll();
    }

    /**
     * 执行设备处理逻辑：自动解析规则内容并处理所有Sensor节点。
     *
     * @param ruleJson 包含规则信息的JSON对象
     */
    public void executeDeviceProcessing(JsonNode ruleJson) {
        if (!ruleJson.has("steps")) {
            System.out.println("规则中未包含有效的步骤信息，跳过设备处理。");
            return;
        }

        ruleJson.fields().forEachRemaining(entry -> {
            JsonNode node = entry.getValue();
            if (node.has("type") && "Sensor".equalsIgnoreCase(node.get("type").asText())) {
                String location = node.get("location").asText();
                String sensingFunction = node.get("sensingFunction").asText();

                System.out.println("处理Sensor节点：位置=" + location + "，功能=" + sensingFunction);

                Optional<SpaceInfo> optionalSpaceInfo = spaceService.findBySpaceName(location);
                if (optionalSpaceInfo.isEmpty()) {
                    System.out.println("未找到位置=" + location + "的空间信息，跳过该节点。");
                    return;
                }

                SpaceInfo spaceInfo = optionalSpaceInfo.get();
                Set<DeviceInfo> devices = spaceInfo.getSpaceDevices();
                if (devices == null || devices.isEmpty()) {
                    System.out.println("空间：" + location + " 中没有设备，跳过该节点。");
                    return;
                }

                String functionUrl = findFunctionUrl(devices, sensingFunction);
                if (functionUrl == null) {
                    System.out.println("在空间：" + location + "中未找到功能：" + sensingFunction + " 的设备。");
                } else {
                    System.out.println("成功找到功能URL：" + functionUrl);
                }
            }
        });
    }

    /**
     * 在设备列表中查找匹配的功能URL。
     *
     * @param devices          设备列表
     * @param sensingFunction  要匹配的功能名称
     * @return 匹配的功能URL，如果未找到返回null
     */
    private String findFunctionUrl(Set<DeviceInfo> devices, String sensingFunction) {
        for (DeviceInfo device : devices) {
            Set<ActuatingFunctionDevice> actuatingFunctions = device.getActuatingFunctions();
            if (actuatingFunctions == null) {
                continue;
            }

            for (ActuatingFunctionDevice afd : actuatingFunctions) {
                ActuatingFunctionInfo afi = afd.getActuatingFunction();
                if (afi != null && sensingFunction.equalsIgnoreCase(afi.getName())) {
                    System.out.println("找到设备：" + device.getDeviceName() + "，功能：" + afi.getName());
                    return afd.getUrl(); // 从 ActuatingFunctionDevice 获取 URL
                }
            }
        }
        return null;
    }
}