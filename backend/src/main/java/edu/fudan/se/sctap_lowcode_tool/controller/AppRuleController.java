package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.AppRuleRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.service.AppRuleService;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.MilvusUtil;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.entity.AppRuleRecord;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/taps")
public class AppRuleController {

    @Autowired
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

}
