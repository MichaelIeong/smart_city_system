package edu.fudan.se.sctap_lowcode_tool.controller;

import com.alibaba.dashscope.exception.NoApiKeyException;
import edu.fudan.se.sctap_lowcode_tool.DTO.AppRuleRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.RecommendRequest;
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

import static edu.fudan.se.sctap_lowcode_tool.constant.Prompt.SYSTEM_PROMPT;

@RestController
@RequestMapping("/api/taps")
public class AppRuleController {

    @Autowired
    private AppRuleService appRuleService;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private MilvusUtil milvusUtil;

    private Map<String, List<Message>> messageMap = new HashMap<>();

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
        messageMap.remove(uuid);
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

    @PostMapping("/recommend/regenerate")
    public ResponseEntity<String> regenerate(@RequestBody RecommendRequest recommendRequest) {
        String uuid = recommendRequest.getUuid();
        String message = recommendRequest.getMessage();
        List<Message> defaultMessages = new ArrayList<>();
        defaultMessages.add(new SystemMessage(SYSTEM_PROMPT));
        List<Message> messages = messageMap.getOrDefault(uuid, defaultMessages);
        messages.add(new UserMessage(message));
        ChatResponse response = chatModel.call(new Prompt(messages));
        messages.add(response.getResult().getOutput());
        messageMap.put(uuid, messages);
        return ResponseEntity.ok(response.getResult().getOutput().getText());
    }

    @PostMapping("/recommend/generate")
    public ResponseEntity<AppRuleInfo> generate(@RequestBody RecommendRequest recommendRequest) throws NoApiKeyException {
        String message = recommendRequest.getMessage();
        List<AppRuleRecord> records = milvusUtil.queryVector(message, 1);
        AppRuleInfo appRuleInfo = null;
        if (records.size() > 0) {
            AppRuleRecord record = records.get(0);
            appRuleInfo = appRuleService.getRuleById(Integer.parseInt(record.getId())).get();
        }
        return ResponseEntity.ok(appRuleInfo);
    }

}
