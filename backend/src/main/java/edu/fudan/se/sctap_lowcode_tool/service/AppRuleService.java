package edu.fudan.se.sctap_lowcode_tool.service;

import com.alibaba.dashscope.exception.NoApiKeyException;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.*;
import edu.fudan.se.sctap_lowcode_tool.constant.SystemPrompt;
import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.AppRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.ProjectRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.MilvusUtil;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.entity.AppRuleRecord;
import edu.fudan.se.sctap_lowcode_tool.utils.redis.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    private final ChatLanguageModel chatLanguageModel;

    @Resource
    private RedisUtil redisUtil;

    @Resource(name = "ruleExecutor")
    private Executor ruleExecutor;

    // 记录每个uuid的对话历史
    private final Map<String, List<ChatMessage>> uuidMessageHistoryMap = new HashMap<>();
    // 记录每个uuid最新对话时间
    private final Map<String, Long> uuidUpdateTimeMap = new HashMap<>();
//    // 记录处于等待中的应用规则
//    Map<String, Set<String>> eventWaitMap = new HashMap<>();
//
//    private final ObjectMapper objectMapper = new ObjectMapper();

    public AppRuleService(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
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

    public ResponseEntity<String> generateNaturalRule(RuleGenerateRequest ruleGenerateRequest) {
        String uuid = ruleGenerateRequest.getUuid();
        String message = ruleGenerateRequest.getMessage();
        // 更新 uuid 的对话时间
        uuidUpdateTimeMap.put(uuid, System.currentTimeMillis());
        // 获取对话历史
        List<ChatMessage> messages = uuidMessageHistoryMap.getOrDefault(uuid, new ArrayList<>());
        // 如果是第一次对话，构造系统消息
        if(messages.isEmpty()) {
            messages.add(new SystemMessage(SystemPrompt.NATURAL_RULE_GENERATE_PROMPT));
        }
        // 加入用户消息
        messages.add(new UserMessage(message));
        // 调用大模型
        ChatResponse response = chatLanguageModel.chat(messages);
        if(response!=null) {
            // 加入 AI 消息，并更新对话历史
            messages.add(response.aiMessage());
            uuidMessageHistoryMap.put(uuid, messages);
            // 返回结果
            String text = response.aiMessage().text();
            return ResponseEntity.ok(text);
        }
        return ResponseEntity.badRequest().body("发生错误，请稍后重试！");
    }

    public ResponseEntity<String> generateJsonRule(RuleGenerateRequest ruleGenerateRequest) {
        String message = ruleGenerateRequest.getMessage();
        // 构造系统消息和用户消息
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage(SystemPrompt.JSON_RULE_GENERATE_PROMPT));
        messages.add(new UserMessage(message));
        // 调用大模型
        ChatResponse response = chatLanguageModel.chat(messages);
        if(response!=null) {
            String text = response.aiMessage().text();
            Matcher matcher = Pattern.compile("```json\\s*(\\{.*?})\\s*```", Pattern.DOTALL).matcher(text);
            // 如果是用 ```json ``` 包围，就提取其中的 JSON 内容
            if(matcher.find()) {
                return ResponseEntity.ok(matcher.group(1).trim());
            }
            return ResponseEntity.ok(text);
        }
        return ResponseEntity.badRequest().body("发生错误，请稍后重试！");
    }

    public ResponseEntity<AppRuleInfo> findSimilarRules(RuleGenerateRequest ruleGenerateRequest) {
        String message = ruleGenerateRequest.getMessage();
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

    public ResponseEntity<String> convertJsonRule(String jsonRule) {
        // 构造系统消息和用户消息
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage(SystemPrompt.JSON_RULE_CONVERT_NODE_RED_PROMPT));
        messages.add(new UserMessage(jsonRule));
        // 调用大模型
        ChatResponse response = chatLanguageModel.chat(messages);
        if(response!=null) {
            String text = response.aiMessage().text();
            Matcher matcher = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```").matcher(text);
            // 如果是用 ```json ``` 包围，就提取其中的 JSON 内容
            if(matcher.find()) {
                return ResponseEntity.ok(matcher.group(1).trim());
            }
            return ResponseEntity.ok(text);
        }
        return ResponseEntity.badRequest().body("发生错误，请稍后重试！");
    }

//    public ResponseEntity<String> generateJsonRule(RecommendRequest recommendRequest) {
//        String uuid = recommendRequest.getUuid();
//        String message = recommendRequest.getMessage();
//        List<ChatMessage> messages = new ArrayList<>();
//        // 构造系统提示词
//        List<AppRuleData> appRuleDataList = ruleDataMap.get(uuid);
//        if(appRuleDataList==null){
//            return ResponseEntity.badRequest().body("找不到uuid");
//        }
//        AppRuleData appRuleData = null;
//        for(AppRuleData data:appRuleDataList){
//            if(data.getRule().equals(message)){
//                appRuleData = data;
//                break;
//            }
//        }
//        if(appRuleData==null){
//            return ResponseEntity.badRequest().body("找不到message");
//        }
//        List<String> eventList = redisUtil.getMulti(appRuleData.getComponents().getEventType(), RedisConstant.Event_Prefix);
//        List<String> propertyList = redisUtil.getMulti(appRuleData.getComponents().getPropertyType(),  RedisConstant.Property_Prefix);
//        List<String> actionList = redisUtil.getMulti(appRuleData.getComponents().getActionType(), RedisConstant.Action_Prefix);
//        String eventOptions = String.join("\n", eventList);
//        String propertyOptions = String.join("\n", propertyList);
//        String actionOptions = String.join("\n", actionList);
//        String systemPrompt = String.format(SystemPrompt.SIMPLE_RULE_PROMPT, eventOptions, propertyOptions, actionOptions);
//        // 加入系统消息
//        messages.add(new SystemMessage(systemPrompt));
//        // 加入用户输入的消息
//        messages.add(new UserMessage(message));
//        // 规定输出的格式为 JSON
//        ChatResponse response = chatLanguageModel.chat(messages);
//        if (response != null) {
//            String text = response.aiMessage().text();
//            Matcher matcher = Pattern.compile("```json\\s*(\\{.*?})\\s*```", Pattern.DOTALL).matcher(text);
//            if (matcher.find()) {
//                return ResponseEntity.ok(matcher.group(1).trim());
//            }
//        }
//        return ResponseEntity.badRequest().body("发生错误，请稍后再试！");
//    }


//    public  ResponseEntity<String> convertComplexJsonRule(AppRuleRequest appRuleRequest) {
//        String ruleJson = appRuleRequest.ruleJson();
//        // 构建系统消息和用户消息
//        List<ChatMessage> messages = new ArrayList<>();
//        messages.add(new SystemMessage(SystemPrompt.COMPLEX_RULE_CONVERT_PROMPT));
//        messages.add(new UserMessage(ruleJson));
//        // 规定输出的格式为 JSON
//        ChatResponse response = chatLanguageModel.chat(messages);
//        if (response != null) {
//            String text = response.aiMessage().text();
//            Matcher matcher = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```").matcher(text);
//            if (matcher.find()) {
//                return ResponseEntity.ok(matcher.group(1).trim());
//            }
//        }
//        return ResponseEntity.badRequest().body("发生错误，请稍后再试！");
//    }
//
//    public ResponseEntity<String> generateNaturalRule(RecommendRequest recommendRequest){
//        String uuid = recommendRequest.getUuid();
//        String message = recommendRequest.getMessage();
//        // 更新uuid的时间戳
//        uuidTimeMap.put(uuid, System.currentTimeMillis());
//        // 获取内存中的消息
//        List<ChatMessage> messages = messageMap.getOrDefault(uuid, new ArrayList<>());
//        // 如果内存中不存在就构建消息
//        if(messages.isEmpty()){
//            // 从redis中获取系统提示词
//            String systemPrompt = redisUtil.getSingle(RedisConstant.NATURAL_PROMPT);
//            if(systemPrompt==null){
//                // 构建提示词
//                List<String> eventList = redisUtil.getAll(RedisConstant.Event_Prefix);
//                List<String> propertyList = redisUtil.getAll(RedisConstant.Property_Prefix);
//                List<String> actionList = redisUtil.getAll(RedisConstant.Action_Prefix);
//                String eventOptions    = String.join("\n", eventList);
//                String propertyOptions = String.join("\n", propertyList);
//                String actionOptions   = String.join("\n", actionList);
//                systemPrompt = String.format(SystemPrompt.SIMPLE_NATURAL_RULE_PROMPT, eventOptions, propertyOptions, actionOptions);
//                // 存入redis
//                redisUtil.setSingle(RedisConstant.NATURAL_PROMPT, systemPrompt);
//            }
//            messages.add(new SystemMessage(systemPrompt));
//        }
//        // 将用户输入的消息加入
//        messages.add(new UserMessage(message));
//        ChatResponse response = chatLanguageModel.chat(messages);
//        // 解析输出的内容
//        String jsonContent;
//        if (response != null) {
//            // 加入消息
//            messages.add(response.aiMessage());
//            messageMap.put(uuid, messages);
//            jsonContent = response.aiMessage().text();
//            Pattern pattern = Pattern.compile("```json\\s*(\\{[\\s\\S]*?\\})\\s*```");
//            Matcher matcher = pattern.matcher(jsonContent);
//            // 如果匹配到，提取 JSON 内容
//            if (matcher.find()) {
//                jsonContent = matcher.group(1).trim();
//            }
//            AppRuleData appRuleData;
//            try{
//                appRuleData = objectMapper.readValue(jsonContent, AppRuleData.class);
//            } catch (Exception e){
//                return ResponseEntity.badRequest().body("输出格式错误，请稍后重试");
//            }
//            // 加入规则和对应的事件、动作、属性
//            List<AppRuleData> appRuleDataList = ruleDataMap.getOrDefault(uuid, new ArrayList<>());
//            appRuleDataList.add(appRuleData);
//            ruleDataMap.put(uuid, appRuleDataList);
//            return ResponseEntity.ok(appRuleData.getRule());
//        }
//        return ResponseEntity.badRequest().body("发生错误，请稍后再试！");
//    }

//    public void triggerAppRule(EventTriggerDTO eventTriggerDTO) {
//        // TODO，暂时使用示例JSON模拟
//        String json = JsonExample.json;
//        // 解析JSON规则
//        AppRule appRule;
//        try {
//            appRule = objectMapper.readValue(json, AppRule.class);
//        } catch (JsonProcessingException e) {
//            log.error("解析JSON规则失败: {}", e.getMessage());
//            return;
//        }
//        // 提取参数event_type
//        String eventType = eventTriggerDTO.getEvent_type();
//        Event event = null;
//        for (Event e : appRule.getTrigger().getEvent()) {
//            if(e.getEvent_type().equals(eventType)) {
//                event = e;
//                break;
//            }
//        }
//        if(event==null){
//            log.error("未找到对应的事件类型");
//            return;
//        }
//        // 判断应用是否处于等待中
//        Set<String> waitSet = eventWaitMap.get(eventType);
//        if(waitSet!=null) {
//            for(String value : eventTriggerDTO.getParams().values()) {
//                if(waitSet.contains(value)) {
//                    log.info("应用处于等待中");
//                    return;
//                }
//            }
//        }
//        // 提取参数params
//        Map<String, Object> params = new HashMap<>();
//        for(Map.Entry<String, String> entry: event.getParams().entrySet()){
//            String paramName = entry.getKey();
//            String paramType = entry.getValue();
//            switch (paramType) {
//                case "string":
//                    params.put(paramName, eventTriggerDTO.getParams().get(paramName));
//                    break;
//                case "number":
//                    params.put(paramName, Integer.parseInt(eventTriggerDTO.getParams().get(paramName)));
//                    break;
//                case "bool":
//                    params.put(paramName, Boolean.parseBoolean(eventTriggerDTO.getParams().get(paramName)));
//                    break;
//                default:
//                    params.put(paramName, eventTriggerDTO.getParams().get(paramName));
//                    log.error("不支持的类型: {}", paramType);
//            }
//        }
//        // TODO, 将事件存入数据库中
//        // 处理response
//        Response response = appRule.getResponse();
//        // response从chain开始
//        if(response.isChainType()) {
//            List<ChainStep> chain = response.getChain();
//            // 提交线程池处理
//            ruleExecutor.execute(() -> {
//                try {
//                    handleChain(chain, eventType, params);
//                } catch (Exception e) {
//                    log.error("处理 chain 失败: {}", e.getMessage());
//                }
//            });
//        }
//        // response从branch开始
//        if(response.isBranchType()) {
//            List<BranchNode> branchNodes = response.getBranch();
//            BranchStep branchStep = new BranchStep();
//            branchStep.setBranch(branchNodes);
//            try {
//                handleBranchStep(branchStep, eventType, params);
//            } catch (Exception e) {
//                log.error("处理 branch 失败: {}", e.getMessage());
//            }
//        }
//    }
//
//    private void handleChain(List<ChainStep> chain, String eventType, Map<String, Object> params) {
//        for(int i=0;i<chain.size();i++) {
//            ChainStep step = chain.get(i);
//            switch (step) {
//                case ActionStep actionStep -> handleActionStep(actionStep, eventType, params);
//                case WaitStep waitStep -> {
//                    // 处理到 wait 后停止
//                    handleWaitStep(waitStep, eventType, params, chain, i);
//                    return;
//                }
//                case BranchStep branchStep -> handleBranchStep(branchStep, eventType, params);
//                default -> log.warn("未知的 ChainStep 类型: {}", step.getClass().getName());
//            }
//        }
//    }
//
//    private void handleActionStep(ActionStep actionStep, String eventType, Map<String, Object> params){
//        // TODO，这里暂时模拟执行动作
//        ActionStep.Action action = actionStep.getAction();
//        log.info("执行动作：{}, 地点：{}, 事件类型：{}", action.getAction_name(), params.get("location"), eventType);
//    }
//
//    private void handleBranchStep(BranchStep branchStep, String eventType, Map<String, Object> params) {
//        List<BranchNode> branchNodes = branchStep.getBranch();
//        for(BranchNode branchNode : branchNodes) {
//            if(branchNode.isCurrentCondition()) {
//                // 处理current_condition
//                if(checkCurrentCondition(branchNode.getEffectiveCondition(), eventType, params)) {
//                    // 提交线程池处理
//                    ruleExecutor.execute(() -> {
//                        try {
//                            handleChain(branchNode.getChain(), eventType, params);
//                        } catch (Exception e) {
//                            log.error("处理 chain 失败: {}", e.getMessage());
//                        }
//                    });
//                }
//            }
//            if(branchNode.isHistoryCondition()) {
//                // 处理history_condition
//                if(checkHistoryCondition(branchNode.getEffectiveCondition(), eventType, params)) {
//                    // 提交线程池处理
//                    ruleExecutor.execute(() -> {
//                        try {
//                            handleChain(branchNode.getChain(), eventType, params);
//                        } catch (Exception e) {
//                            log.error("处理 chain 失败: {}", e.getMessage());
//                        }
//                    });
//                }
//            }
//        }
//    }
//
//    private boolean checkCurrentCondition(List<Condition> currentConditions, String eventType, Map<String, Object> params){
//        for(Condition condition : currentConditions) {
//            String left = condition.getLeft().getValue();
//            String[] parts = left.split("\\.");
//            String leftProperty = parts.length > 1 ? parts[1] : parts[0];
//            String location = params.get("location").toString();
//            //TODO，需要查数据库获取当前位置的属性值,这是暂时随机
//            Random random = new Random();
//            int leftValue = random.nextInt(3);
//            String operator = condition.getOperator();
//            String right = condition.getRight();
//            int rightValue = Integer.parseInt(right);
//            if(compareLeftAndRight(leftValue, rightValue, operator)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private boolean checkHistoryCondition(List<Condition> historyConditions, String eventType, Map<String, Object> params){
//        for(Condition condition : historyConditions) {
//            Condition.Func left = condition.getLeft().getFunc();
//            String func = left.getFunc();
//            Map<String, String> funcParams = left.getParams();
//            String regex = "(\\w+)\\(([^)]+)\\)";
//            Pattern pattern = Pattern.compile(regex);
//            Matcher matcher = pattern.matcher(func);
//            if(matcher.find()) {
//                String functionName = matcher.group(1);
//                String parameters = matcher.group(2);
//                String[] paramArray = parameters.split(",\\s*");
//                if(paramArray.length != 3) {
//                    log.error("无效的参数形式：{}", func);
//                    return false;
//                }
//                String duration = paramArray[1];
//                String unit = paramArray[2];
//                //TODO，需要查数据库查询历史事件，暂时用随机数代替
//                Random random = new Random();
//                int leftValue = random.nextInt(3);
//                String operator = condition.getOperator();
//                String right = condition.getRight();
//                int rightValue = Integer.parseInt(right);
//                if(compareLeftAndRight(leftValue, rightValue, operator)) {
//                    return false;
//                }
//            }
//            else {
//                log.error("无效的函数形式：{}", func);
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private boolean compareLeftAndRight(int leftValue, int rightValue, String operator) {
//        switch (operator) {
//            case "=", "==" -> {
//                return leftValue != rightValue;
//            }
//            case "!=" -> {
//                return leftValue == rightValue;
//            }
//            case "<" -> {
//                return leftValue >= rightValue;
//            }
//            case ">" -> {
//                return leftValue <= rightValue;
//            }
//            case "<=" -> {
//                return leftValue > rightValue;
//            }
//            case ">=" -> {
//                return leftValue < rightValue;
//            }
//            default -> {
//                log.error("未知运算符: {}", operator);
//                return true;
//            }
//        }
//    }
//
//    private void handleWaitStep(WaitStep waitStep, String eventType, Map<String, Object> params, List<ChainStep> chain, int index) {
//        // 将应用加入等待
//        WaitStep.Wait wait = waitStep.getWait();
//        Set<String> waitSet = eventWaitMap.getOrDefault(eventType, new HashSet<>());
//        Map<String, String> waitParams;
//        if(wait.isActionCondition()) {
//            waitParams = wait.getAction_condition().getParams();
//        }
//        else {
//            waitParams = wait.getTime_condition().getParams();
//        }
//        String waitKey = waitParams.entrySet().iterator().next().getValue();
//        String waitValue = params.get(waitKey).toString();
//        waitSet.add(waitValue);
//        eventWaitMap.put(eventType, waitSet);
//        // 如无特殊情况，wait 是 chain 的最后一个步骤
//        if(chain.size() == index + 1) {
//            Map<String, Object> data = new HashMap<>();
//            data.put("eventType", eventType);
//            data.put("waitValue", waitValue);
//            long currentTimeMillis = System.currentTimeMillis();
//            String redisKey = "";
//            // 处理action_condition
//            if(wait.isActionCondition()) {
//                log.info("事件 {} 加入动作等待中, Value: {}", eventType, waitValue);
//                redisKey = RedisConstant.Action_Wait + eventType + ":" + waitValue;
//                // 这里设定 action_condition 的超时时间为 1 小时
//                long expireTimeMillis = currentTimeMillis + 60 * 60 * 1000L;
//                data.put("expireTime", expireTimeMillis);
//            }
//            // 处理time_condition
//            if(wait.isTimeCondition()) {
//                log.info("事件 {} 加入时间等待, Value: {}", eventType, waitValue);
//                redisKey = RedisConstant.Time_Wait + eventType + ":" + waitValue;
//                // 存储到期时间
//                int waitDuration = Integer.parseInt(wait.getTime_condition().getDuration());
//                String waitUnit = wait.getTime_condition().getUnit();
//                long expireTimeMillis = switch (waitUnit.toLowerCase()) {
//                       case "second", "seconds" -> currentTimeMillis + waitDuration * 1000L;
//                       case "minute", "minutes" -> currentTimeMillis + waitDuration * 60 * 1000L;
//                       case "hour", "hours" -> currentTimeMillis + waitDuration * 60 * 60 * 1000L;
//                       // 默认使用分钟
//                       default -> currentTimeMillis + waitDuration * 60 * 1000L;
//                };
//                data.put("expireTime", expireTimeMillis);
//            }
//            // 存储到 redis 中
//            try {
//                redisUtil.setWait(redisKey, data);
//            } catch (JsonProcessingException e) {
//                log.error("wait 数据序列化失败");
//            }
//        }
//    }
//
//    public void actionComplete(ActionCompleteDTO actionCompleteDTO) {
//        String eventType = actionCompleteDTO.getEvent_type();
//        String waitValue = actionCompleteDTO.getValue();
//        String redisKey = RedisConstant.Action_Wait + eventType + ":" + waitValue;
//        // 从 redis 中删除
//        redisUtil.deleteSingle(redisKey);
//        // 从等待中移除
//        Set<String> waitSet = eventWaitMap.get(eventType);
//        waitSet.remove(waitValue);
//        eventWaitMap.put(eventType, waitSet);
//        log.info("事件 {} 结束动作等待, Value: {}", eventType, waitValue);
//    }

    /**
     * 定时任务：每小时执行一次，清理过期的uuid数据
     */
    public void cleanUpOldData() {
        log.info("开始执行定时清理任务...");
        long now = System.currentTimeMillis();
        long oneHourAgo = now - 3600000; // 1小时之前的时刻
        Iterator<Map.Entry<String, Long>> iterator = uuidUpdateTimeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String uuid = entry.getKey();
            Long timestamp = entry.getValue();
            if (timestamp != null && timestamp < oneHourAgo) {
                // 清理map中的旧数据
                uuidMessageHistoryMap.remove(uuid);
                iterator.remove(); // 同时移除时间戳记录
                log.info("清理 uuid 对应的数据: {}", uuid);
            }
        }
    }
//
//    /**
//     * 定时任务：每隔30s执行一次，检查到期的chain并执行
//     * */
//    public void checkExpiredChain() {
//        log.info("开始检查到期的 wait 应用...");
//        // 获取所有以 timeCondition 前缀开头的 key 对应的值
//        List<String> waits = redisUtil.getAll(RedisConstant.Time_Wait);
//        if(waits == null || waits.isEmpty()) {
//            log.info("没有待检查的 wait 应用...");
//            return;
//        }
//        long now = System.currentTimeMillis();
//        for(String wait : waits) {
//            try {
//                if(wait.trim().isEmpty()) {
//                    continue;
//                }
//                // 反序列化
//                Map waitData = objectMapper.readValue(wait, Map.class);
//                long expireTime = Long.parseLong(waitData.get("expireTime").toString());
//                if(now >= expireTime) {
//                    String eventType = waitData.get("eventType").toString();
//                    String waitValue = waitData.get("waitValue").toString();
//                    String redisKey = RedisConstant.Time_Wait + eventType + ":" + waitValue;
//                    // 从 redis 中删除
//                    redisUtil.deleteSingle(redisKey);
//                    // 从等待中移除
//                    Set<String> waitSet = eventWaitMap.get(eventType);
//                    waitSet.remove(waitValue);
//                    eventWaitMap.put(eventType, waitSet);
//                    log.info("事件 {} 结束时间等待, Value: {}", eventType, waitValue);
//                }
//            } catch (Exception e) {
//                log.error("反序列化 wait 数据失败：{}", e.getMessage());
//            }
//        }
//    }
}