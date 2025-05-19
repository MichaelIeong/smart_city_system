package edu.fudan.se.sctap_lowcode_tool.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.AppRuleData;
import edu.fudan.se.sctap_lowcode_tool.DTO.AppRuleRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.RecommendRequest;
import edu.fudan.se.sctap_lowcode_tool.constant.Redis_Constant;
import edu.fudan.se.sctap_lowcode_tool.constant.Sys_Prompt;
import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.service.AppRuleService;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.MilvusUtil;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.entity.AppRuleRecord;
import edu.fudan.se.sctap_lowcode_tool.utils.redis.RedisUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/taps")
public class AppRuleController {
    @Resource
    private AppRuleService appRuleService;
    @Resource
    private MilvusUtil milvusUtil;
    @Resource
    private RedisUtil redisUtil;
    private final ChatClient chatClient;
    // 保存生成自然语言规则消息
    private Map<String, List<Message>> messageMap = new HashMap<>();
    // 保存自然语言规则和对应的事件、属性、动作
    private Map<String, List<AppRuleData>> ruleDataMap = new HashMap<>();
    // 记录每个uuid最后的访问时间
    private Map<String, Long> uuidTimeMap = new HashMap<>();
    @Autowired
    public AppRuleController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

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
        String uuid = recommendRequest.getUuid();
        String message = recommendRequest.getMessage();
        List<Message> messages = new ArrayList<>();
        // 构造系统提示词
        List<AppRuleData> appRuleDataList = ruleDataMap.get(uuid);
        if(appRuleDataList==null){
            return ResponseEntity.badRequest().body("找不到uuid");
        }
        AppRuleData appRuleData = null;
        for(AppRuleData data:appRuleDataList){
            if(data.getRule().equals(message)){
                appRuleData = data;
                break;
            }
        }
        if(appRuleData==null){
            return ResponseEntity.badRequest().body("找不到message");
        }
        List<String> eventList = redisUtil.getMulti(appRuleData.getComponents().getEventType(), Redis_Constant.Event_Prefix);
        List<String> propertyList = redisUtil.getMulti(appRuleData.getComponents().getPropertyType(),  Redis_Constant.Property_Prefix);
        List<String> actionList = redisUtil.getMulti(appRuleData.getComponents().getActionType(), Redis_Constant.Action_Prefix);
        String eventOptions = String.join("\n", eventList);
        String propertyOptions = String.join("\n", propertyList);
        String actionOptions = String.join("\n", actionList);
        String systemPrompt = String.format(Sys_Prompt.SYSTEM_PROMPT2, eventOptions, propertyOptions, actionOptions);
        // 加入系统消息
        messages.add(new SystemMessage(systemPrompt));
        // 加入用户输入的消息
        messages.add(new UserMessage(message));
        Prompt prompt = new Prompt(messages);
        // 规定输出的格式为 JSON
        ChatResponse response = chatClient.prompt(prompt)
                .call()
                .chatResponse();
        String jsonRule = response.getResult().getOutput().getText();
        Pattern pattern = Pattern.compile("```json\\s*(\\{.*?})\\s*```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(jsonRule);
        if (matcher.find()) {
            jsonRule = matcher.group(1);
        }
        return ResponseEntity.ok(jsonRule.trim());
    }

    /**
     * 从向量数据库中匹配
     * */
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

    /**
     * 生成自然语言规则
     * */
    @PostMapping("/recommend/generateNaturalRule")
    public ResponseEntity<String> generateNaturalRule(@RequestBody RecommendRequest recommendRequest){
        String uuid = recommendRequest.getUuid();
        String message = recommendRequest.getMessage();
        // 更新uuid的时间戳
        uuidTimeMap.put(uuid, System.currentTimeMillis());
        // 获取内存中的消息
        List<Message> messages = messageMap.getOrDefault(uuid, new ArrayList<>());
        // 如果内存中不存在就构建消息
        if(messages.isEmpty()){
            // 从redis中获取系统提示词
            String systemPrompt = redisUtil.getSingle(Redis_Constant.SYSTEM_PROMPT1);
            if(systemPrompt==null){
                // 构建提示词
                List<String> eventList = redisUtil.getAll(Redis_Constant.Event_Prefix);
                List<String> propertyList = redisUtil.getAll(Redis_Constant.Property_Prefix);
                List<String> actionList = redisUtil.getAll(Redis_Constant.Action_Prefix);
                String eventOptions    = String.join("\n", eventList);
                String propertyOptions = String.join("\n", propertyList);
                String actionOptions   = String.join("\n", actionList);
                systemPrompt = String.format(Sys_Prompt.SYSTEM_PROMPT1, eventOptions, propertyOptions, actionOptions);
                // 存入redis
                redisUtil.setSingle(Redis_Constant.SYSTEM_PROMPT1, systemPrompt);
            }
            messages.add(new SystemMessage(systemPrompt));
        }
        // 将用户输入的消息加入
        messages.add(new UserMessage(message));
        Prompt prompt = new Prompt(messages);
        // 规定输出的格式为 JSON
        DashScopeResponseFormat responseFormat = new DashScopeResponseFormat();
        responseFormat.setType(DashScopeResponseFormat.Type.JSON_OBJECT);
        ChatResponse response = chatClient.prompt(prompt)
                .options(
                        DashScopeChatOptions.builder()
                                .withResponseFormat(responseFormat)
                                .build()
                )
                .call()
                .chatResponse();
        // 解析输出的内容
        String jsonContent = response.getResult().getOutput().getText();
        AppRuleData appRuleData;
        try{
            ObjectMapper mapper = new ObjectMapper();
            appRuleData = mapper.readValue(jsonContent, AppRuleData.class);
        } catch (Exception e){
            return ResponseEntity.badRequest().body("输出格式错误，请稍后重试");
        }
        // 加入消息
        messages.add(response.getResult().getOutput());
        messageMap.put(uuid, messages);
        // 加入规则和对应的事件、动作、属性
        List<AppRuleData> appRuleDataList = ruleDataMap.getOrDefault(uuid, new ArrayList<>());
        appRuleDataList.add(appRuleData);
        ruleDataMap.put(uuid, appRuleDataList);
        return ResponseEntity.ok(appRuleData.getRule());
    }

    /**
     * 定时任务：每小时执行一次，清理过期的uuid数据
     */
    public void cleanUpOldData() {
        System.out.println("开始执行定时清理任务...");
        long now = System.currentTimeMillis();
        long oneHourAgo = now - 3600000; // 1小时之前的时刻
        Iterator<Map.Entry<String, Long>> iterator = uuidTimeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String uuid = entry.getKey();
            Long timestamp = entry.getValue();
            if (timestamp != null && timestamp < oneHourAgo) {
                // 清理三个map中的旧数据
                messageMap.remove(uuid);
                ruleDataMap.remove(uuid);
                iterator.remove(); // 同时移除时间戳记录
                System.out.println("清理 uuid 对应的数据: " + uuid);
            }
        }
    }

}