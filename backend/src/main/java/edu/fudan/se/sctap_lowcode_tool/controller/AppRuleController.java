package edu.fudan.se.sctap_lowcode_tool.controller;

import com.alibaba.dashscope.exception.NoApiKeyException;
import edu.fudan.se.sctap_lowcode_tool.DTO.AppRuleRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.RecommendRequest;
import edu.fudan.se.sctap_lowcode_tool.constant.enums.MessageTypeEnum;
import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.model.MessageInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.MessageRepository;
import edu.fudan.se.sctap_lowcode_tool.service.AppRuleService;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.MilvusUtil;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.entity.AppRuleRecord;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

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

    @Autowired
    private MessageRepository messageRepository;

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
        // 根据 uuid 查询数据库中已有的消息数量
        long existingMessagesCount = messageRepository.countByUuid(uuid);
        // 将消息存入数据库
        List<Message> messages = messageMap.getOrDefault(uuid, new ArrayList<>());
        if(messages.size()>existingMessagesCount){
            // 只保存后面的消息
            for (int i = (int) existingMessagesCount; i < messages.size(); i++) {
                Message message = messages.get(i);
                MessageInfo messageInfo = new MessageInfo();
                messageInfo.setUuid(uuid);
                messageInfo.setContent(message.getText());
                messageInfo.setCreateTime(LocalDateTime.now());

                // 根据消息类型设置 messageType
                switch (message.getMessageType()) {
                    case USER -> messageInfo.setMessageType(MessageTypeEnum.USER_TYPE.getCode());
                    case ASSISTANT -> messageInfo.setMessageType(MessageTypeEnum.ASSISTANT_TYPE.getCode());
                    case SYSTEM -> messageInfo.setMessageType(MessageTypeEnum.SYSTEM_TYPE.getCode());
                    case TOOL -> messageInfo.setMessageType(MessageTypeEnum.TOOL_TYPE.getCode());
                }
                // 保存到数据库
                messageRepository.save(messageInfo);
            }
        }
        // 本地删除
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
        // 获取内存中的消息
        List<Message> messages = messageMap.getOrDefault(uuid, new ArrayList<>());
        // 如果内存中不存在就查数据库
        if(messages.isEmpty()){
            List<MessageInfo> messageInfos = messageRepository.findAllMessagesByUuidOrderedById(uuid);
            // 如果数据库中不存在就构建系统消息
            if(messageInfos.isEmpty()){
                messages.add(new SystemMessage(SYSTEM_PROMPT));
            }
            // 如果数据库中存在就添加消息
            else{
                for (MessageInfo messageInfo : messageInfos) {
                    if (messageInfo.getMessageType() == MessageTypeEnum.USER_TYPE.getCode()) {
                        messages.add(new UserMessage(messageInfo.getContent()));
                    } else if (messageInfo.getMessageType() == MessageTypeEnum.ASSISTANT_TYPE.getCode()) {
                        messages.add(new AssistantMessage(messageInfo.getContent()));
                    } else if (messageInfo.getMessageType() == MessageTypeEnum.SYSTEM_TYPE.getCode()) {
                        messages.add(new SystemMessage(messageInfo.getContent()));
                    }
                }
            }
        }
        // 将用户输入的消息加入
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