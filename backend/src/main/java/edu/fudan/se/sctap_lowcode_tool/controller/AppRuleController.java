package edu.fudan.se.sctap_lowcode_tool.controller;

import com.alibaba.dashscope.exception.NoApiKeyException;
import edu.fudan.se.sctap_lowcode_tool.DTO.AppRuleRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.RecommendRequest;
import edu.fudan.se.sctap_lowcode_tool.constant.Sys_Prompt;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/taps")
public class AppRuleController {

    @Autowired
    private AppRuleService appRuleService;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private MilvusUtil milvusUtil;


    // 保存生成自然语言规则消息
    private Map<String, List<Message>> messageMap1 = new HashMap<>();

    // 保存生成 tap 规则消息
    private Map<String, List<Message>> messageMap2 = new HashMap<>();

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
        String uuid = rule.uuid();
        // 本地删除
        messageMap1.remove(uuid);
        messageMap2.remove(uuid);
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

    @PostMapping("/recommend/generateJsonRule")
    public ResponseEntity<String> generateJsonRule(@RequestBody RecommendRequest recommendRequest) {
        String uuid = recommendRequest.getUuid();
        String message = recommendRequest.getMessage();
        // 获取内存中的消息
        List<Message> messages = messageMap2.getOrDefault(uuid, new ArrayList<>());
        // 如果内存中不存在就构建消息
        if(messages.isEmpty()){
            messages.add(new SystemMessage(Sys_Prompt.SYSTEM_PROMPT2));
        }
        // 将用户输入的消息加入
        messages.add(new UserMessage(message));
        ChatResponse response = chatModel.call(new Prompt(messages));
        messages.add(response.getResult().getOutput());
        messageMap2.put(uuid, messages);
        return ResponseEntity.ok(response.getResult().getOutput().getText());
    }

    @PostMapping("/recommend/findSimilarRule")
    public ResponseEntity<AppRuleInfo> findSimilarRules(@RequestBody RecommendRequest recommendRequest) throws NoApiKeyException {
        String message = recommendRequest.getMessage();
        List<AppRuleRecord> records = milvusUtil.queryVector(message, 1);
        AppRuleInfo appRuleInfo = null;
        if (records.size() > 0) {
            AppRuleRecord record = records.get(0);
            appRuleInfo = appRuleService.getRuleById(Integer.parseInt(record.getId())).get();
        }
        return ResponseEntity.ok(appRuleInfo);
    }

    @PostMapping("/recommend/generateNaturalRule")
    public ResponseEntity<String> generateNaturalRule(@RequestBody RecommendRequest recommendRequest){
        String uuid = recommendRequest.getUuid();
        String message = recommendRequest.getMessage();
        // 获取内存中的消息
        List<Message> messages = messageMap1.getOrDefault(uuid, new ArrayList<>());
        // 如果内存中不存在就构建消息
        if(messages.isEmpty()){
            messages.add(new SystemMessage(Sys_Prompt.SYSTEM_PROMPT1));
        }
        // 将用户输入的消息加入
        messages.add(new UserMessage(message));
        ChatResponse response = chatModel.call(new Prompt(messages));
        messages.add(response.getResult().getOutput());
        messageMap1.put(uuid, messages);
        return ResponseEntity.ok(response.getResult().getOutput().getText());
    }

}