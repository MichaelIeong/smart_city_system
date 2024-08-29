package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import edu.fudan.se.sctap_lowcode_tool.model.RuleInfo;
import edu.fudan.se.sctap_lowcode_tool.service.FusionService;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/fusion")
@Tag(name = "FusionController", description = "事件融合控制器")
public class FusionController {

    @Autowired
    private FusionService fusionService;

    Gson gson = new Gson();

    String projectName = "test";

    int ruleStatus = 0;

    int callCount = 2;

    @Operation(summary = "上传新的规则", description = "用户在node-red构建好规则，传给后端，加入到数据库")
    @PostMapping("/uploadrule")
    public ResponseEntity<Void> saveRule(@RequestBody Map<String, JsonNode> msg){
        JsonNode ruleJson = msg.get("ruleJson");
        JsonNode flowJson = msg.get("flowJson");
        RuleInfo ruleInfo = new RuleInfo();
        ruleInfo.setFlowJson(flowJson.toString());
        ruleInfo.setRuleJson(ruleJson.toString());
        ruleInfo.setRuleName(ruleJson.get("rulename").toString());
        ruleInfo.setProjectName(projectName);
        ruleInfo.setRuleStatus(ruleStatus);
        ruleInfo.setCallCount(callCount);
        System.out.println(flowJson.toString());
        System.out.println(ruleJson.toString());
        System.out.println(ruleJson.get("rulename").toPrettyString());
        fusionService.addNewRule(ruleInfo);
        return ResponseEntity.ok().build();

    }

}
