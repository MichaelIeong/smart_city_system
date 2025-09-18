package edu.fudan.se.sctap_lowcode_tool.service;

import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.*;
import edu.fudan.se.sctap_lowcode_tool.DTO.APPRULE.*;
import edu.fudan.se.sctap_lowcode_tool.constant.SystemPrompt;
import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.AppRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.ProjectRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.MilvusUtil;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.entity.AppRuleRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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

    // 记录每个uuid的对话历史
    private final Map<String, List<ChatMessage>> uuidMessageHistoryMap = new HashMap<>();
    // 记录每个uuid最新对话时间
    private final Map<String, Long> uuidUpdateTimeMap = new HashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public AppRuleInfo getAppRuleById(Integer id) {
        AppRuleInfo appRuleInfo = appRuleRepository.findById(id).orElse(null);
        if(appRuleInfo!=null) {
            // 判断flowJson是否为空
            if(appRuleInfo.getFlowJson()==null||appRuleInfo.getFlowJson().isBlank()) {
                // 使用大模型转换
                String flowJson = convertAppRuleJsonToNodeRedFlowJson(appRuleInfo.getRuleJson());
                if(flowJson==null) {
                    return null;
                }
                appRuleInfo.setFlowJson(flowJson);
                appRuleRepository.save(appRuleInfo);
            }
            return appRuleInfo;
        }
        return null;
    }

    public void deleteRulesByIds(Iterable<Integer> ruleIds) {
        appRuleRepository.deleteAllById(ruleIds);
        // 从向量数据库中删除
        for (Integer id : ruleIds) {
            milvusUtil.deleteRecordById(id.toString());
        }
    }

    public boolean createRule(AppRuleSaveRequest appRuleSaveRequest) {
        String ruleJson = appRuleSaveRequest.getRuleJson();
        String flowJson = appRuleSaveRequest.getFlowJson();
        // 如果是大模型创建应用
        if(ruleJson!=null&&!ruleJson.isBlank()){
            AppRule appRule = parseJsonRule(ruleJson);
            if (appRule != null) {
                var appRuleInfo = getEntityFromRequest(appRuleSaveRequest);
                // 设置事件类型
                appRuleInfo.setEventType(appRule.getTrigger().getEvent_type());
                // 插入数据库
                appRuleInfo = appRuleRepository.save(appRuleInfo);
                // 插入向量数据库
                if(appRuleInfo.getDescription()!=null&&!appRuleInfo.getDescription().isBlank()) {
                    AppRuleRecord record = new AppRuleRecord(appRuleInfo.getId().toString(), appRuleSaveRequest.getDescription());
                    try{
                        milvusUtil.insertRecord(record);
                    } catch (NoApiKeyException e) {
                        log.error("No api key");
                    }
                }
                return true;
            }
            return false;
        }
        // 如果是通过Node-RED创建应用
        if(flowJson!=null&&!flowJson.isBlank()) {
            // 转换为JSON
            String jsonRule = convertNodeRedFlowJsonToAppRuleJson(flowJson);
            if(jsonRule==null||jsonRule.isBlank()) {
                return false;
            }
            var appRuleInfo = getEntityFromRequest(appRuleSaveRequest);
            appRuleInfo.setRuleJson(jsonRule);
            var appRule = parseJsonRule(jsonRule);
            appRuleInfo.setEventType(appRule.getTrigger().getEvent_type());
            // 插入数据库
            appRuleInfo = appRuleRepository.save(appRuleInfo);
            // 插入向量数据库
            if(appRuleInfo.getDescription()!=null&&!appRuleInfo.getDescription().isBlank()) {
                AppRuleRecord record = new AppRuleRecord(appRuleInfo.getId().toString(), appRuleSaveRequest.getDescription());
                try{
                    milvusUtil.insertRecord(record);
                } catch (NoApiKeyException e) {
                    log.error("No api key");
                }
            }
            return true;
        }
        return false;
    }

    public boolean updateRule(AppRuleUpdateRequest appRuleUpdateRequest) {
        // 首先判断应用是否存在
        AppRuleInfo appRuleInfo = appRuleRepository.findById(appRuleUpdateRequest.getId()).orElse(null);
        if(appRuleInfo==null) {
            return false;
        }
        String flowJson = appRuleUpdateRequest.getFlowJson();
        String description = appRuleUpdateRequest.getDescription();
        if(flowJson!=null&&!flowJson.isBlank()) {
            // 更新 flowJson 和 ruleJson
            if(!flowJson.equals(appRuleInfo.getFlowJson())) {
                // 转换JSON
                String jsonRule = convertNodeRedFlowJsonToAppRuleJson(flowJson);
                if(jsonRule==null||jsonRule.isBlank()) {
                    return false;
                }
                var appRule = parseJsonRule(jsonRule);
                appRuleInfo.setRuleJson(jsonRule);
                appRuleInfo.setEventType(appRule.getTrigger().getEvent_type());
                appRuleInfo.setFlowJson(flowJson);
            }
            // 更新 description
            if(!description.equals(appRuleInfo.getDescription())) {
                appRuleInfo.setDescription(description);
                // 更新向量数据库
                try{
                    milvusUtil.deleteRecordById(appRuleInfo.getId().toString());
                    if(!description.isBlank()) {
                        AppRuleRecord record = new AppRuleRecord(appRuleInfo.getId().toString(), appRuleUpdateRequest.getDescription());
                        milvusUtil.insertRecord(record);
                    }
                } catch (NoApiKeyException e) {
                    log.error("No api key");
                }
            }
            appRuleInfo.setUpdateTime(LocalDateTime.now());
            appRuleRepository.save(appRuleInfo);
            return true;
        }
        return false;
    }

    private AppRuleInfo getEntityFromRequest(AppRuleSaveRequest appRuleSaveRequest) {
        AppRuleInfo appRuleInfo = new AppRuleInfo();
        projectRepository.findById(appRuleSaveRequest.getProjectId()).ifPresentOrElse(
                appRuleInfo::setProject,
                () -> {
                    throw new BadRequestException(
                            "400", "Project not found",
                            "rule.projectId", appRuleSaveRequest.getProjectId().toString(), "projectId not found"
                    );
                });
        appRuleInfo.setDescription(appRuleSaveRequest.getDescription());
        appRuleInfo.setRuleJson(appRuleSaveRequest.getRuleJson());
        appRuleInfo.setFlowJson(appRuleSaveRequest.getFlowJson());
        appRuleInfo.setUpdateTime(LocalDateTime.now());
        return appRuleInfo;
    }

    public PageDTO<AppRuleInfo> list(Integer projectId, AppRuleQueryRequest appRuleQueryRequest) {
        Pageable pageable = PageRequest.of(appRuleQueryRequest.getPageNo() - 1, appRuleQueryRequest.getPageSize(), Sort.by("id").ascending());
        Page<AppRuleInfo> repoResult = appRuleRepository.searchByProjectWithFilters(projectId, appRuleQueryRequest.getEventType(), appRuleQueryRequest.getDescription(), pageable);
        return new PageDTO<>(
                appRuleQueryRequest.getPageNo(), appRuleQueryRequest.getPageSize(),
                repoResult.getTotalElements(), repoResult.getTotalPages(),
                repoResult.getContent()
        );
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
            AppRuleRecord record = records.get(0);
            appRuleInfo = getRuleById(Integer.parseInt(record.getId())).get();
        }
        return ResponseEntity.ok(appRuleInfo);
    }

    /**
     * 将AppRuleJson 转换为 Node-RED Flow JSON
     * */
    public String convertAppRuleJsonToNodeRedFlowJson(String jsonRule) {
        // 构造系统消息和用户消息
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage(SystemPrompt.JSON_RULE_CONVERT_NODE_RED_PROMPT));
        messages.add(new UserMessage(jsonRule));
        // 最多重试一次
        final int MAX_RETRY = 1;
        for(int attempt=0; attempt<=MAX_RETRY; attempt++) {
            ChatResponse response;
            String rawText;
            try {
                // 调用大模型
                response = chatLanguageModel.chat(messages);
                if (response == null || response.aiMessage() == null) {
                    appendFeedback(messages,
                            "模型未返回有效响应（response/aiMessage 为 null）。请仅输出一个 ```json ... ``` 代码块，内容为符合约束的 Node-RED Flow JSON。");
                    continue;
                }
                rawText = response.aiMessage().text();
                if (rawText == null || rawText.isBlank()) {
                    appendFeedback(messages,
                            "模型返回文本为空。请仅输出一个 ```json ... ``` 代码块，且不要包含解释性文字。");
                    continue;
                }
                Matcher matcher = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```").matcher(rawText);
                // 提取JSON
                if (!matcher.find()) {
                    appendFeedback(messages,
                            "未在输出中找到 ```json ... ``` 代码块。请仅输出一个 ```json ... ``` 代码块，且内容必须是单个 JSON 对象。");
                    continue;
                }
                return matcher.group(1).trim();
            } catch (Exception callEx) {
                appendFeedback(messages,
                        "调用模型异常：" + callEx.getMessage() + "。请仅输出一个 ```json ... ``` 代码块的有效 Node-RED Flow JSON。");
            }
        }
        return null;
    }

    /**
     * 解析JSON规则
     * */
    public AppRule parseJsonRule(String jsonRule) {
        AppRule appRule;
        try {
            appRule = objectMapper.readValue(jsonRule, AppRule.class);
            return appRule;
        } catch (JsonProcessingException e) {
            log.error("解析JSON规则失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将Node-RED Flow 转换成 AppRuleJson
     * */
    private String convertNodeRedFlowJsonToAppRuleJson(String nodeRedFlowJson) {
        // 构造系统消息和用户消息
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage(SystemPrompt.NODE_RED_CONVERT_JSON_RULE_PROMPT));
        messages.add(new UserMessage(nodeRedFlowJson));
        // 最多重试一次
        final int MAX_RETRY = 1;
        for(int attempt=0; attempt<=MAX_RETRY; attempt++) {
            ChatResponse response;
            String rawText;
            try{
                // 调用大模型
                response = chatLanguageModel.chat(messages);
                if (response == null || response.aiMessage() == null) {
                    appendFeedback(messages,
                            "模型未返回有效响应（response/aiMessage 为 null）。请仅输出一个 ```json ... ``` 代码块，内容为符合约束的 AppRule JSON。");
                    continue;
                }
                rawText = response.aiMessage().text();
                if (rawText == null || rawText.isBlank()) {
                    appendFeedback(messages,
                            "模型返回文本为空。请仅输出一个 ```json ... ``` 代码块，且不要包含解释性文字。");
                    continue;
                }
                Matcher matcher = Pattern.compile("```json\\s*(\\{.*?})\\s*```", Pattern.DOTALL).matcher(rawText);
                // 提取JSON
                if (!matcher.find()) {
                    appendFeedback(messages,
                            "未在输出中找到 ```json ... ``` 代码块。请仅输出一个 ```json ... ``` 代码块，且内容必须是单个 JSON 对象。");
                    continue;
                }
                String jsonRule = matcher.group(1).trim();
                // 判断能否被解析
                try {
                    AppRule appRule = parseJsonRule(jsonRule);
                    if (appRule != null) {
                        // 通过：返回原始 JSON 字符串
                        return jsonRule;
                    } else {
                        appendFeedback(messages,
                                "解析失败：parseJsonRule 返回 null。请根据提示修正并仅输出一个 ```json ... ``` 代码块的有效 AppRule JSON。");
                    }
                } catch (Exception parseEx) {
                    appendFeedback(messages,
                            "解析异常：" + parseEx.getMessage() + "。请修正并仅输出一个 ```json ... ``` 代码块的有效 AppRule JSON。");
                }

            } catch (Exception callEx) {
                appendFeedback(messages,
                        "调用模型异常：" + callEx.getMessage() + "。请仅输出一个 ```json ... ``` 代码块的有效 AppRule JSON。");
            }
        }
        // 全部尝试失败
        return null;
    }

    /**
     * 将失败原因作为「用户消息」追加到同一个 messages 中，引导模型按要求重试。
     */
    private void appendFeedback(List<ChatMessage> messages, String reason) {
        String guidance =
                "上一次输出有问题：" + reason + "\n\n" +
                        "请严格按照以下要求重新生成：\n" +
                        "1) 仅输出一个代码块，形如：```json\\n{...}\\n```\n" +
                        "2) 代码块内容必须是**单个 JSON 对象**（不能是数组或多段）。\n" +
                        "3) 不要在代码块外输出任何解释、注释或额外文本。\n" +
                        "4) 严格满足系统提示（JSON 结构与字段约束）。";
        messages.add(new UserMessage(guidance));
    }

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
}
