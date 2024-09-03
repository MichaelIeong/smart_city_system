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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/fusion")
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
        System.out.println("上传新规则/更新规则");
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

    @Operation(summary = "获取规则列表", description = "将规则list传给前端")
    @PostMapping("/getRulelist")
    public ResponseEntity<List<RuleInfo>> getRuleList(){
        System.out.println("获取规则列表");
        List<RuleInfo> ruleInfoList = new ArrayList<>();
        ruleInfoList = fusionService.getRuleList();
        System.out.println(ruleInfoList);
        return ResponseEntity.ok(ruleInfoList);

    }

}
