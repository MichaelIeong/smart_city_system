package edu.fudan.se.sctap_lowcode_tool.service;

import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.*;
import edu.fudan.se.sctap_lowcode_tool.DTO.app.*;
import edu.fudan.se.sctap_lowcode_tool.constant.Json_Example;
import edu.fudan.se.sctap_lowcode_tool.constant.Redis_Constant;
import edu.fudan.se.sctap_lowcode_tool.constant.Sys_Prompt;
import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.AppRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.ProjectRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.MilvusUtil;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.entity.AppRuleRecord;
import edu.fudan.se.sctap_lowcode_tool.utils.redis.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AppRuleService {

    @Resource
    private AppRuleRepository appRuleRepository;

    @Resource
    private ProjectRepository projectRepository;

    @Resource
    private MilvusUtil milvusUtil;

    private final ChatClient chatClient;

    @Resource
    private RedisUtil redisUtil;

    @Resource(name = "ruleExecutor")
    private Executor ruleExecutor;

    // 保存生成自然语言规则消息
    private final Map<String, List<Message>> messageMap = new HashMap<>();
    // 保存自然语言规则和对应的事件、属性、动作
    private final Map<String, List<AppRuleData>> ruleDataMap = new HashMap<>();
    // 保存复杂规则生成对话消息
    private final Map<String, List<Message>> complexMessageMap = new HashMap<>();
    // 记录每个uuid最后的访问时间
    private final Map<String, Long> uuidTimeMap = new HashMap<>();
    // 维护event_type和ignoreLocations的映射关系
    Map<String, Set<String>> ignoreLocationsMap = new HashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AppRuleService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public PageDTO<AppRuleInfo> getAllRulesByProjectId(Integer projectId, int pageNo, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);
        Page<AppRuleInfo> repoResult = appRuleRepository.findAllByProjectId(projectId, pageRequest);
        return new PageDTO<>(
                pageNo, pageSize,
                repoResult.getTotalElements(), repoResult.getTotalPages(),
                repoResult.getContent()
        );
    }

    public Optional<AppRuleInfo> getRuleById(Integer ruleId) {
        return appRuleRepository.findById(ruleId);
    }

    public void deleteRulesByIds(Iterable<Integer> ruleIds) {
        appRuleRepository.deleteAllById(ruleIds);
        // 从向量数据库中删除
        for (Integer id : ruleIds) {
            milvusUtil.deleteRecordById(id.toString());
        }
    }

    public void createRule(AppRuleRequest rule) {
        var appRuleInfo = getEntityFromRequest(rule);
        appRuleInfo = appRuleRepository.save(appRuleInfo);
        // 加入向量数据库
        AppRuleRecord record = new AppRuleRecord(appRuleInfo.getId().toString(), rule.description());
        try{
            milvusUtil.insertRecord(record);
        } catch (NoApiKeyException e) {
            log.error("No api key");
        }
    }

    public void updateRule(Integer ruleId, AppRuleRequest rule) {
        var appRuleInfo = getEntityFromRequest(rule);
        if (appRuleRepository.findById(ruleId).isEmpty()) {
            throw new BadRequestException(
                    "400", "Rule not exists to update",
                    "rule.id", ruleId.toString(), "ruleId not found"
            );
        }
        appRuleInfo.setId(ruleId);
        appRuleRepository.save(appRuleInfo);
        // 更新向量数据库
        milvusUtil.deleteRecordById(ruleId.toString());
        AppRuleRecord record = new AppRuleRecord(ruleId.toString(), rule.description());
        try{
            milvusUtil.insertRecord(record);
        } catch (NoApiKeyException e) {
            log.error("No api key found");
        }
    }

    private AppRuleInfo getEntityFromRequest(AppRuleRequest rule) {
        AppRuleInfo appRuleInfo = new AppRuleInfo();
        projectRepository.findById(rule.projectId()).ifPresentOrElse(
                appRuleInfo::setProject,
                () -> {
                    throw new BadRequestException(
                            "400", "Project not found",
                            "rule.projectId", rule.projectId().toString(), "projectId not found"
                    );
                });
        appRuleInfo.setDescription(rule.description());
        appRuleInfo.setRuleJson(rule.ruleJson());
        appRuleInfo.setUpdateTime(LocalDateTime.now());
        return appRuleInfo;
    }

    public ResponseEntity<String> generateJsonRule(RecommendRequest recommendRequest) {
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
        String systemPrompt = String.format(Sys_Prompt.SIMPLE_RULE_PROMPT, eventOptions, propertyOptions, actionOptions);
        // 加入系统消息
        messages.add(new SystemMessage(systemPrompt));
        // 加入用户输入的消息
        messages.add(new UserMessage(message));
        Prompt prompt = new Prompt(messages);
        // 规定输出的格式为 JSON
        ChatResponse response = chatClient.prompt(prompt)
                .call()
                .chatResponse();
        if (response != null) {
            String text = response.getResult().getOutput().getText();
            Matcher matcher = Pattern.compile("```json\\s*(\\{.*?})\\s*```", Pattern.DOTALL).matcher(text);
            if (matcher.find()) {
                return ResponseEntity.ok(matcher.group(1).trim());
            }
        }
        return ResponseEntity.badRequest().body("发生错误，请稍后再试！");
    }

    public ResponseEntity<String> generateComplexJsonRule(RecommendRequest recommendRequest) {
        String uuid = recommendRequest.getUuid();
        String message = recommendRequest.getMessage();
        // 更新uuid的时间戳
        uuidTimeMap.put(uuid, System.currentTimeMillis());
        // 获取内存中的消息
        List<Message> messages = complexMessageMap.getOrDefault(uuid, new ArrayList<>());
        // 如果内存中不存在就构建消息
        if(messages.isEmpty()){
            String systemPrompt = Sys_Prompt.COMPLEX_RULE_PROMPT;
            messages.add(new SystemMessage(systemPrompt));
        }
        // 将用户输入的消息加入
        messages.add(new UserMessage(message));
        Prompt prompt = new Prompt(messages);
        // 规定输出的格式为 JSON
        ChatResponse response = chatClient.prompt(prompt)
                .call()
                .chatResponse();
        if (response != null) {
            messages.add(response.getResult().getOutput());
            complexMessageMap.put(uuid, messages);
            String text = response.getResult().getOutput().getText();
            Matcher matcher = Pattern.compile("```json\\s*(\\{.*?})\\s*```", Pattern.DOTALL).matcher(text);
            if (matcher.find()) {
                return ResponseEntity.ok(matcher.group(1).trim());
            }
        }
        return ResponseEntity.badRequest().body("发生错误，请稍后再试！");
    }

    public ResponseEntity<String> generateNaturalRule(RecommendRequest recommendRequest){
        String uuid = recommendRequest.getUuid();
        String message = recommendRequest.getMessage();
        // 更新uuid的时间戳
        uuidTimeMap.put(uuid, System.currentTimeMillis());
        // 获取内存中的消息
        List<Message> messages = messageMap.getOrDefault(uuid, new ArrayList<>());
        // 如果内存中不存在就构建消息
        if(messages.isEmpty()){
            // 从redis中获取系统提示词
            String systemPrompt = redisUtil.getSingle(Redis_Constant.NATURAL_PROMPT);
            if(systemPrompt==null){
                // 构建提示词
                List<String> eventList = redisUtil.getAll(Redis_Constant.Event_Prefix);
                List<String> propertyList = redisUtil.getAll(Redis_Constant.Property_Prefix);
                List<String> actionList = redisUtil.getAll(Redis_Constant.Action_Prefix);
                String eventOptions    = String.join("\n", eventList);
                String propertyOptions = String.join("\n", propertyList);
                String actionOptions   = String.join("\n", actionList);
                systemPrompt = String.format(Sys_Prompt.NATURAL_RULE_PROMPT, eventOptions, propertyOptions, actionOptions);
                // 存入redis
                redisUtil.setSingle(Redis_Constant.NATURAL_PROMPT, systemPrompt);
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
        String jsonContent;
        if (response != null) {
            // 加入消息
            messages.add(response.getResult().getOutput());
            messageMap.put(uuid, messages);
            jsonContent = response.getResult().getOutput().getText();
            AppRuleData appRuleData;
            try{
                appRuleData = objectMapper.readValue(jsonContent, AppRuleData.class);
            } catch (Exception e){
                return ResponseEntity.badRequest().body("输出格式错误，请稍后重试");
            }
            // 加入规则和对应的事件、动作、属性
            List<AppRuleData> appRuleDataList = ruleDataMap.getOrDefault(uuid, new ArrayList<>());
            appRuleDataList.add(appRuleData);
            ruleDataMap.put(uuid, appRuleDataList);
            return ResponseEntity.ok(appRuleData.getRule());
        }
        return ResponseEntity.badRequest().body("发生错误，请稍后再试！");
    }

    public ResponseEntity<AppRuleInfo> findSimilarRules(RecommendRequest recommendRequest) {
        String message = recommendRequest.getMessage();
        List<AppRuleRecord> records;
        try{
            records = milvusUtil.queryVector(message, 1);
        } catch (NoApiKeyException e){
            return ResponseEntity.badRequest().build();
        }
        AppRuleInfo appRuleInfo = null;
        if (!records.isEmpty()) {
            AppRuleRecord record = records.getFirst();
            appRuleInfo = getRuleById(Integer.parseInt(record.getId())).get();
        }
        return ResponseEntity.ok(appRuleInfo);
    }

    public void triggerAppRule(EventTriggerDTO eventTriggerDTO) {
        // TODO，暂时使用示例JSON模拟
        String json = Json_Example.json;
        // 解析JSON规则
        AppRule appRule;
        try {
            appRule = objectMapper.readValue(json, AppRule.class);
        } catch (JsonProcessingException e) {
            log.error("解析JSON规则失败", e);
            return;
        }
        // 提取参数
        String eventType = eventTriggerDTO.getEvent_type();
        Event  event = null;
        for(Event e: appRule.getTrigger().getEvent()){
            if(e.getEvent_type().equals(eventType)){
                event = e;
                break;
            }
        }
        if(event==null){
            log.error("未找到对应的事件类型");
            return;
        }
        Map<String, Object> params = new HashMap<>();
        for(Map.Entry<String, String> entry: event.getParams().entrySet()){
            String key = entry.getKey();
            params.put(key, eventTriggerDTO.getParams().get(key));
        }
        // 处理filter
        if(!isFilterSatisfied(appRule.getTrigger().getFilter(), params, eventType)){
            return;
        }
        // 处理response
        Response response = appRule.getResponse();
        // response从chain开始
        if(response.isChainType()){
            List<ChainStep> chain = response.getChain();
            //提交线程池处理
            ruleExecutor.execute(() -> {
                try {
                    handleChain(chain, params, eventType);
                } catch (Exception e) {
                    log.error("线程池执行出错: {}", e.getMessage());
                }
            });
            return;
        }
        // response从branch开始
        if(response.isBranchType()){
            List<BranchNode> branch = response.getBranch();
            BranchStep branchStep = new BranchStep();
            branchStep.setBranch(branch);
            try{
                handleBranchStep(branchStep, params, eventType);
            } catch (Exception e) {
                log.error("处理branch出错", e);
            }
        }
    }

    public void actionComplete(ActionCompleteDTO actionCompleteDTO) {
        String eventType = actionCompleteDTO.getEvent_type();
        String location = actionCompleteDTO.getLocation();
        String redisKey = Redis_Constant.Action_Condition + eventType + ":" + location;
        // 从redis中获取chain
        String data = redisUtil.getSingle(redisKey);
        if(data==null){
            return;
        }
        // 从redis中删除
        redisUtil.deleteSingle(redisKey);
        try{
            Map<String, Object> dataMap = objectMapper.readValue(data, new TypeReference<>() {});
            List<ChainStep> chain = objectMapper.convertValue(dataMap.get("chain"), new TypeReference<>() {});
            Map<String, Object> params = objectMapper.convertValue(dataMap.get("params"), new TypeReference<>() {});
            // 加入线程池处理
            ruleExecutor.execute(() -> {
                try {
                    handleChain(chain, params, eventType);
                } catch (Exception e) {
                    log.error("线程池执行出错: {}", e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("解析chain失败: {}", e.getMessage());
        }
    }

    //判断过滤条件是否满足
    private boolean isFilterSatisfied(List<Map<String, Object>> filters,Map<String, Object> params, String eventType){
        if(filters == null || filters.isEmpty()){
            //无过滤器默认通过
            return true;
        }
        for(Map<String, Object> filter : filters){
            //处理location
            if(filter.containsKey("location")){
                Map<String, Object> locationFilter = (Map<String, Object>) filter.get("location");
                String operator = (String) locationFilter.get("locationOperator");
                String targetLocation = (String) locationFilter.get("targetLocation");
                String currentLocation = (String) params.get("location");
                if(!checkLocationCondition(operator,targetLocation,currentLocation, eventType)){
                    return false;
                }
            }
            // 处理time
            if(filter.containsKey("time")) {
                Map<String, Object> timeFilter = (Map<String, Object>) filter.get("time");
                String operator = (String) timeFilter.get("timeOperator");
                String targetTime = (String) timeFilter.get("targetTime");
                if(!checkTimeCondition(operator, targetTime)) {
                    return false;
                }
            }
        }
        return true;
    }

    //检查位置条件
    private boolean checkLocationCondition(String operator, String targetLocation, String currentLocation, String eventType){
        if(currentLocation == null){
            return false;
        }
        if(operator.equals("not in")) {
            if(targetLocation.contains("ignoreLocations")&&ignoreLocationsMap.containsKey(eventType)) {
                Set<String> ignoreLocations = ignoreLocationsMap.get(eventType);
                return !ignoreLocations.contains(currentLocation);
            }
        }
        if(operator.equals("in")) {
            return currentLocation.equals(targetLocation);
        }
        return false;
    }

    // 检查时间条件
    private boolean checkTimeCondition(String operator, String targetTime){
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime target = LocalTime.parse(targetTime, formatter);
        if("before".equalsIgnoreCase(operator)) {
            return currentTime.isBefore(target);
        }
        else if("after".equalsIgnoreCase(operator)) {
            return currentTime.isAfter(target);
        }
        return false;
    }

    // 处理chain
    private void handleChain(List<ChainStep> chain, Map<String, Object> params, String eventType) throws JsonProcessingException {
        int size = chain.size();
        label:
        for(int i = 0; i < size; i++){
            ChainStep step = chain.get(i);
            switch (step) {
                case ActionStep actionStep:
                    handleActionStep(actionStep, params, eventType);
                    break;
                case WaitStep waitStep:
                    handleWaitStep(waitStep, params, eventType, chain, i);
                    break label; // wait后续不再处理
                case IgnoreStep ignoreStep:
                    handleIgnoreStep(ignoreStep, params, eventType);
                    break;
                case ResumeStep resumeStep:
                    handleResumeStep(resumeStep, params, eventType);
                    break;
                case BranchStep branchStep:
                    handleBranchStep(branchStep, params, eventType);
                    break;
                default:
                    log.warn("未知的 ChainStep 类型: {}", step.getClass().getName());
                    break;
            }
        }
    }

    private void handleActionStep(ActionStep actionStep, Map<String, Object> params, String eventType){
        // TODO，这里暂时模拟执行动作
        log.info("执行动作：{} 地点：{}", actionStep.getAction().getAction_name(), params.get("location"));
    }

    private void handleWaitStep(WaitStep waitStep, Map<String, Object> params, String eventType, List<ChainStep> chain, int index) throws JsonProcessingException {
        // 提取待执行的ChainStep
        List<ChainStep> subChain = chain.subList(index + 1, chain.size());
        // 处理action_condition
        if(waitStep.getWait().isActionCondition()){
            String waitEventType = waitStep.getWait().getAction_condition().getEvent_type();
            String redisKey = Redis_Constant.Action_Condition + waitEventType + ":"+ params.get("location");
            // 存储到redis
            Map<String, Object> data = new HashMap<>();
            data.put("chain", subChain);
            data.put("params", params);
            data.put("eventType", eventType);
            redisUtil.setChain(redisKey, data);
        }
        // 处理time_condition
        if(waitStep.getWait().isTimeCondition()){
            int waitDuration = Integer.parseInt(waitStep.getWait().getTime_condition().getDuration());
            String waitUnit = waitStep.getWait().getTime_condition().getUnit();
            long currentTimeMillis = System.currentTimeMillis();
            long expireTimeMillis = switch (waitUnit.toLowerCase()) {
                case "second" -> currentTimeMillis + waitDuration * 1000L;
                case "minute" -> currentTimeMillis + waitDuration * 60 * 1000L;
                case "hour" -> currentTimeMillis + waitDuration * 60 * 60 * 1000L;
                case "day" -> currentTimeMillis + waitDuration * 24 * 60 * 60 * 1000L;
                default -> throw new IllegalArgumentException("Unsupported time unit: " + waitUnit);
            };
            Map<String, Object> data = new HashMap<>();
            data.put("chain", subChain);
            data.put("expireTime", expireTimeMillis);
            data.put("params", params);
            data.put("eventType", eventType);
            // 存储到redis
            String redisKey = Redis_Constant.Time_Condition + eventType + ":" + params.get("location");
            redisUtil.setChain(redisKey, data);
        }
    }

    private void handleIgnoreStep(IgnoreStep ignoreStep, Map<String, Object> params, String eventType){
        // 添加到ignoreLocationsMap中
        Set<String> ignoreLocations = ignoreLocationsMap.getOrDefault(ignoreStep.getIgnore().getEvent_type(), new HashSet<>());
        ignoreLocations.add((String) params.get("location"));
        ignoreLocationsMap.put(ignoreStep.getIgnore().getEvent_type(), ignoreLocations);
        log.info("ignore: event_type {}, location {}", ignoreStep.getIgnore().getEvent_type(), params.get("location"));
    }

    private void handleResumeStep(ResumeStep resumeStep, Map<String, Object> params, String eventType){
        // 从ignoreLocationMap中移除
        Set<String> ignoreLocations = ignoreLocationsMap.get(resumeStep.getResume().getEvent_type());
        ignoreLocations.remove((String) params.get("location"));
        ignoreLocationsMap.put(resumeStep.getResume().getEvent_type(), ignoreLocations);
        log.info("resume: event_type {}, location {}", resumeStep.getResume().getEvent_type(), params.get("location"));
    }

    private void handleBranchStep(BranchStep branchStep, Map<String, Object> params, String eventType) {
        List<BranchNode> branch = branchStep.getBranch();
        for(BranchNode node : branch){
            if(node.isCurrentCondition()){
                // 处理current_condition
                if(checkCurrentCondition(node.getCurrentCondition(), params,  eventType)) {
                    List<ChainStep> chain = node.getChain();
                    //提交线程池处理
                    ruleExecutor.execute(() -> {
                        try {
                            handleChain(chain, params, eventType);
                        } catch (Exception e) {
                            log.error("线程池执行出错: {}", e.getMessage());
                        }
                    });
                }
            }
            if(node.isHistoryCondition()){
                // 处理history_condition
                if(checkHistoryCondition(node.getHistoryCondition(), params, eventType)) {
                    List<ChainStep> chain = node.getChain();
                    //提交线程池处理
                    ruleExecutor.execute(() -> {
                        try {
                            handleChain(chain, params, eventType);
                        } catch (Exception e) {
                            log.error("线程池执行出错: {}", e.getMessage());
                        }
                    });
                }
            }
        }
    }

    private boolean checkCurrentCondition(List<Condition> currentConditions, Map<String, Object> params, String eventType){
        //TODO，涉及查数据库暂不处理
        for(Condition currentCondition : currentConditions) {
            String left = currentCondition.getLeft();
            String operator = currentCondition.getOperator();
            String right = currentCondition.getRight();
        }
        return true;
    }

    private boolean checkHistoryCondition(List<Condition> historyConditions, Map<String, Object> params, String eventType){
        //TODO，涉及查数据库暂不处理
        for(Condition historyCondition : historyConditions) {
            String left = historyCondition.getLeft();
            String operator = historyCondition.getOperator();
            String right = historyCondition.getRight();
        }
        return true;
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
                complexMessageMap.remove(uuid);
                iterator.remove(); // 同时移除时间戳记录
                System.out.println("清理 uuid 对应的数据: " + uuid);
            }
        }
    }

    /**
     * 定时任务：每隔30s执行一次，检查到期的chain并执行
     * */
    public void checkExpiredChain() {
        System.out.println("开始检查到期的chain...");
        // 获取所有以 timeCondition 前缀开头的 key 对应的值
        List<String> chains = redisUtil.getAll(Redis_Constant.Time_Condition);
        if (chains == null || chains.isEmpty()) return;
        long now = System.currentTimeMillis();
        for (String json : chains) {
            try {
                if(json == null){
                    continue;
                }
                // 反序列化 json 数据
                Map<String, Object> dataMap = objectMapper.readValue(json, new TypeReference<>() {});
                long expireTime = Long.parseLong(dataMap.get("expireTime").toString());
                // 判断是否到期
                if (expireTime <= now) {
                    List<ChainStep> chain = objectMapper.convertValue(dataMap.get("chain"), new TypeReference<>() {});
                    Map<String, Object> params = objectMapper.convertValue(dataMap.get("params"), new TypeReference<>() {});
                    String eventType = (String) dataMap.get("eventType");
                    String location = (String) params.get("location");
                    String redisKey = Redis_Constant.Time_Condition + eventType + ":" + location;
                    // 从redis中删除
                    redisUtil.deleteSingle(redisKey);
                    //提交线程池处理
                    ruleExecutor.execute(() -> {
                        try {
                            handleChain(chain, params, eventType);
                        } catch (Exception e) {
                            log.error("线程池执行出错: {}", e.getMessage());
                        }
                    });
                }
            } catch (Exception e) {
                log.error("解析chain失败: {}", e.getMessage());
            }
        }
    }
}