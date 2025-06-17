package edu.fudan.se.sctap_lowcode_tool.controller;

import com.alibaba.dashscope.exception.NoApiKeyException;
import edu.fudan.se.sctap_lowcode_tool.DTO.AppRuleRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.EventTriggerDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.RecommendRequest;
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
    public void create(@RequestBody AppRuleRequest rule) throws NoApiKeyException {
        appRuleService.createRule(rule);
    }

    @PutMapping("/{id}")
    public void update(
            @PathVariable("id") Integer id,
            @RequestBody AppRuleRequest rule) throws NoApiKeyException {
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
     * 从向量数据库中匹配
     * */
    @PostMapping("/recommend/findSimilarRule")
    public ResponseEntity<AppRuleInfo> findSimilarRules(@RequestBody RecommendRequest recommendRequest) throws NoApiKeyException {
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
    public void triggerAppRule(@RequestBody EventTriggerDTO eventTriggerDTO){
        appRuleService.triggerAppRule(eventTriggerDTO);
    }

}