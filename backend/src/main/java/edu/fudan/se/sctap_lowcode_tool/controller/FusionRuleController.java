package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;
import edu.fudan.se.sctap_lowcode_tool.service.FusionRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
public class FusionRuleController {

    @Autowired
    private FusionRuleService fusionRuleService;

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
}
