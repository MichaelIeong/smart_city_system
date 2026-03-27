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
        return ResponseEntity.ok(appRuleService.getAppRuleById(id));
    }

    /**
     * 保存应用
     * */
    @PostMapping("/create")
    public ResponseEntity<Integer> create(@RequestBody AppRuleSaveRequest appRuleSaveRequest,
                                          @RequestParam(value = "id", required = false) Integer id) {
        return ResponseEntity.ok(appRuleService.createRule(appRuleSaveRequest, id));
    }

    @PostMapping("/update")
    public boolean update(@RequestBody AppRuleUpdateRequest appRuleUpdateRequest) {
        return appRuleService.updateRule(appRuleUpdateRequest);
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
        String flowJson = appRuleService.convertAppRuleJsonToNodeRedFlowJson(jsonRule);
        if(flowJson != null) {
            return ResponseEntity.ok(flowJson);
        }
        return ResponseEntity.badRequest().body("转换失败");
    }

    /**
     * 根据id启用或禁用规则
     * @param id 规则id
     * @param enabled 是否启用
     */
    @PostMapping("/execute/enabled/{id}")
    public boolean updateEnabledStatus(
            @PathVariable Integer id,
            @RequestParam boolean enabled) {
        return appRuleService.updateEnabledStatus(id, enabled);
    }

    /**
     * 同步应用规则
     * */
    @PostMapping("/sync")
    public ResponseEntity<List<AppRuleSyncResponse>> syncAppRule(@RequestBody AppRuleSyncRequest appRuleSyncRequest) {
        return ResponseEntity.ok(appRuleService.syncAppRule(appRuleSyncRequest));
    }

    /**
     * 查看应用执行详情
     * */
    @GetMapping("/execute/detail/{id}")
    public ResponseEntity<List<AppRuleExecuteDetail>> getAppRuleExecuteDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(appRuleService.getAppRuleExecuteDetail(id));
    }
}