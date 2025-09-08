package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.*;
import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.service.AppRuleService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/taps")
public class AppRuleController {
    @Resource
    private AppRuleService appRuleService;

    @GetMapping
    public PageDTO<AppRuleInfo> queryAll(
            @RequestParam(name = "project") Integer projectId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        return appRuleService.getAllRulesByProjectId(projectId, pageNo, pageSize);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppRuleInfo> queryById(
            @PathVariable("id") Integer id) {
        return ResponseEntity.of(appRuleService.getRuleById(id));
    }

    /**
     * 保存应用
     * */
    @PostMapping
    public boolean create(@RequestBody AppRuleSaveRequest appRuleSaveRequest) {
        return appRuleService.createRule(appRuleSaveRequest);
    }

    @PutMapping("/{id}")
    public void update(
            @PathVariable("id") Integer id,
            @RequestBody AppRuleRequest rule) {
        appRuleService.updateRule(id, rule);
    }

    /**
     * 删除应用
     * */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Integer id) {
        appRuleService.deleteRulesByIds(List.of(id));
    }

    @DeleteMapping
    public void deleteAll(@RequestParam("id") List<Integer> ids) {
        appRuleService.deleteRulesByIds(ids);
    }

    /**
     * 分页查询
     * */
    @PostMapping("/list/{projectId}")
    public PageDTO<AppRuleInfo> list(
            @PathVariable Integer projectId,
            @RequestBody AppRuleQueryRequest appRuleQueryRequest
    ) {
        return appRuleService.list(projectId, appRuleQueryRequest);
    }

    /**
     * 生成自然语言规则
     * */
    @PostMapping("/recommend/generateNaturalRule")
    public ResponseEntity<String> generateNaturalRule(@RequestBody RuleGenerateRequest ruleGenerateRequest){
        return appRuleService.generateNaturalRule(ruleGenerateRequest);
    }

    /**
     * 生成JSON规则
     * */
    @PostMapping("/recommend/generateJsonRule")
    public ResponseEntity<String> generateJsonRule(@RequestBody RuleGenerateRequest ruleGenerateRequest) {
        return appRuleService.generateJsonRule(ruleGenerateRequest);
    }

    /**
     * 从向量数据库中匹配
     * */
    @PostMapping("/recommend/findSimilarRule")
    public ResponseEntity<AppRuleInfo> findSimilarRules(@RequestBody RuleGenerateRequest ruleGenerateRequest) {
        return appRuleService.findSimilarRules(ruleGenerateRequest);
    }

    /**
     * 复杂应用json规则node red转换
     * */
    @PostMapping("/recommend/convertJsonRule")
    public ResponseEntity<String> convertJsonRule(@RequestBody String jsonRule) {
        return appRuleService.convertJsonRule(jsonRule);
    }


//    /**
//     * 触发应用规则
//     * */
//    @PostMapping("/trigger")
//    public void triggerAppRule(@RequestBody EventTriggerDTO eventTriggerDTO) {
//        appRuleService.triggerAppRule(eventTriggerDTO);
//    }
//
//    /**
//     * 动作完成上报
//     * */
//    @PostMapping("/action/complete")
//    public void actionComplete(@RequestBody ActionCompleteDTO actionCompleteDTO) {
//        appRuleService.actionComplete(actionCompleteDTO);
//    }

}