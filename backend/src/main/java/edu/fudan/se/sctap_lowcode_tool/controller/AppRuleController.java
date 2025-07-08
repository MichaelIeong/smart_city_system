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

    @PostMapping
    public void create(@RequestBody AppRuleRequest rule) {
        appRuleService.createRule(rule);
    }

    @PutMapping("/{id}")
    public void update(
            @PathVariable("id") Integer id,
            @RequestBody AppRuleRequest rule) {
        appRuleService.updateRule(id, rule);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Integer id) {
        appRuleService.deleteRulesByIds(List.of(id));
    }

    @DeleteMapping
    public void deleteAll(@RequestParam("id") List<Integer> ids) {
        appRuleService.deleteRulesByIds(ids);
    }

    /**
     * 生成json规则
     * */
    @PostMapping("/recommend/generateJsonRule")
    public ResponseEntity<String> generateJsonRule(@RequestBody RecommendRequest recommendRequest) {
        return appRuleService.generateJsonRule(recommendRequest);
    }

    /**
     * 复杂应用json规则生成
     * */
    @PostMapping("/recommend/generateComplexJsonRule")
    public ResponseEntity<String> generateComplexJsonRule(@RequestBody RecommendRequest recommendRequest) {
        return appRuleService.generateComplexJsonRule(recommendRequest);
    }

    /**
     * 复杂应用json规则node red转换
     * */
    @PostMapping("/recommend/convertComplexJsonRule")
    public ResponseEntity<String> convertComplexJsonRule(@RequestBody AppRuleRequest appRuleRequest) {
        return appRuleService.convertComplexJsonRule(appRuleRequest);
    }

    /**
     * 从向量数据库中匹配
     * */
    @PostMapping("/recommend/findSimilarRule")
    public ResponseEntity<AppRuleInfo> findSimilarRules(@RequestBody RecommendRequest recommendRequest) {
        return appRuleService.findSimilarRules(recommendRequest);
    }

    /**
     * 生成自然语言规则
     * */
    @PostMapping("/recommend/generateNaturalRule")
    public ResponseEntity<String> generateNaturalRule(@RequestBody RecommendRequest recommendRequest){
        return appRuleService.generateNaturalRule(recommendRequest);
    }

    /**
     * 触发应用规则
     * */
    @PostMapping("/trigger")
    public void triggerAppRule(@RequestBody EventTriggerDTO eventTriggerDTO) {
        appRuleService.triggerAppRule(eventTriggerDTO);
    }

    /**
     * 动作完成上报
     * */
    @PostMapping("/action/complete")
    public void actionComplete(@RequestBody ActionCompleteDTO actionCompleteDTO) {
        appRuleService.actionComplete(actionCompleteDTO);
    }

}