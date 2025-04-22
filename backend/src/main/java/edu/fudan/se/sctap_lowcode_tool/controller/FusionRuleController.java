package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;
import edu.fudan.se.sctap_lowcode_tool.service.FusionRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/fusion")
@Tag(name = "FusionController", description = "事件融合控制器")
public class FusionRuleController {

    @Autowired
    private FusionRuleService fusionRuleService;

    @Operation(summary = "获取规则列表", description = "将规则列表传给前端")
    @GetMapping("/getRuleList")
    public ResponseEntity<List<FusionRule>> getRuleList() {
        List<FusionRule> fusionRuleList = fusionRuleService.getRuleList();
        return ResponseEntity.ok(fusionRuleList);
    }

    @Operation(summary = "执行规则", description = "激活并执行指定规则")
    @PostMapping("/executeRule/{ruleId}")
    public ResponseEntity<String> executeRuleById(@PathVariable int ruleId) {
        try {
            boolean executed = fusionRuleService.executeRuleById(ruleId);
            if (executed) {
                return ResponseEntity.ok("执行成功");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("规则未找到");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("执行失败：" + e.getMessage());
        }
    }

    @Operation(summary = "暂停规则", description = "将指定规则设为 inactive")
    @PutMapping("/pauseRule/{ruleId}")
    public ResponseEntity<String> pauseRuleById(@PathVariable int ruleId) {
        boolean paused = fusionRuleService.pauseRuleById(ruleId);
        if (paused) {
            return ResponseEntity.ok("暂停成功");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("规则未找到或无法暂停");
        }
    }

    @Operation(summary = "删除规则", description = "根据 ruleId 删除规则")
    @DeleteMapping("/deleteRule/{ruleId}")
    public ResponseEntity<String> deleteRuleById(@PathVariable int ruleId) {
        boolean deleted = fusionRuleService.deleteRuleById(ruleId);
        if (deleted) {
            return ResponseEntity.ok("删除成功");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("规则未找到");
        }
    }
}