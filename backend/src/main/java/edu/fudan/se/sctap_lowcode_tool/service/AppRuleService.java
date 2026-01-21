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
import edu.fudan.se.sctap_lowcode_tool.model.AppGrid;
import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.model.GridMesh;
import edu.fudan.se.sctap_lowcode_tool.repository.*;
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
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AppRuleService {

    @Resource
    private AppRuleRepository appRuleRepository;

    @Resource
    private ProjectRepository projectRepository;

    @Resource
    private EnvServiceService envServiceService;

    @Resource
    private EnvPropertyService envPropertyService;

    @Resource
    private EnvEventService envEventService;

    @Resource
    private MilvusUtil milvusUtil;

    @Resource
    private GridMeshRepository gridMeshRepository;

    @Resource
    private AppGridRepository appGridRepository;

    @Resource
    private TslDeviceRepository tslDeviceRepository;

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
        // 删除边缘应用
        for(Integer ruleId : ruleIds) {
            List<AppGrid> appGrids = appGridRepository.findByAppRuleId(ruleId);
            if(!appGrids.isEmpty()) {
                appGridRepository.deleteAll(appGrids);
            }
            // TODO 下发删除命令
        }
        appRuleRepository.deleteAllById(ruleIds);
        // 从向量数据库中删除
        for (Integer id : ruleIds) {
            milvusUtil.deleteRecordById(id.toString());
        }
    }

    public Integer createRule(AppRuleSaveRequest appRuleSaveRequest) {
        String ruleJson = appRuleSaveRequest.getRuleJson();
        String flowJson = appRuleSaveRequest.getFlowJson();
        String gridId = appRuleSaveRequest.getGridId();
        // 如果是大模型创建应用
        if(ruleJson!=null&&!ruleJson.isBlank()){
            AppRule appRule = parseJsonRule(ruleJson);
            if (appRule != null) {
                var appRuleInfo = getEntityFromRequest(appRuleSaveRequest);
                // 设置事件类型
                appRuleInfo.setEventType(appRule.getTrigger().getEvent_type());
                // 判断是否跨区域
                appRuleInfo.setCrossRegion("crossRegion".equals(gridId));
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
                // 如果不跨区域，绑定应用id和网格id
                if(!"crossRegion".equals(gridId)) {
                    AppGrid appGrid = new AppGrid();
                    appGrid.setAppRuleId(appRuleInfo.getId());
                    appGrid.setGridId(gridId);
                    appGrid.setEnabled(true);
                    appGridRepository.save(appGrid);
                }
                return appRuleInfo.getId();
            }
            return 0;
        }
        // 如果是通过Node-RED创建应用
        if(flowJson!=null&&!flowJson.isBlank()) {
            // 转换为JSON
            String jsonRule = convertNodeRedFlowJsonToAppRuleJson(flowJson, gridId);
            if(jsonRule==null||jsonRule.isBlank()) {
                return 0;
            }
            var appRuleInfo = getEntityFromRequest(appRuleSaveRequest);
            appRuleInfo.setRuleJson(jsonRule);
            var appRule = parseJsonRule(jsonRule);
            appRuleInfo.setEventType(appRule.getTrigger().getEvent_type());
            // 判断是否跨区域
            appRuleInfo.setCrossRegion("crossRegion".equals(gridId));
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
            // 如果不跨区域，绑定应用id和网格id
            if(!"crossRegion".equals(gridId)) {
                AppGrid appGrid = new AppGrid();
                appGrid.setAppRuleId(appRuleInfo.getId());
                appGrid.setGridId(gridId);
                appGrid.setEnabled(true);
                appGridRepository.save(appGrid);
            }
            return appRuleInfo.getId();
        }
        return 0;
    }

    /**
     * 绑定规则到网格
     * 如果创建时指定了 gridId，则自动在 app_grid 表中创建关联
     */
    private void bindRuleToGrid(Integer ruleId, String gridId) {
        if (ruleId != null && gridId != null && !gridId.isBlank()) {
            // 简单处理：直接保存关联，默认启用
            AppGrid appGrid = new AppGrid();
            appGrid.setAppRuleId(ruleId);
            appGrid.setGridId(gridId);
            appGrid.setEnabled(true);
            appGridRepository.save(appGrid);
        }
    }

    public boolean updateRule(AppRuleUpdateRequest appRuleUpdateRequest) {
        // 首先判断应用是否存在
        AppRuleInfo appRuleInfo = appRuleRepository.findById(appRuleUpdateRequest.getId()).orElse(null);
        if(appRuleInfo==null) {
            return false;
        }
        String gridId;
        if(appRuleInfo.getCrossRegion()) {
            gridId = "crossRegion";
        } else {
            List<AppGrid> appGrids = appGridRepository.findByAppRuleId(appRuleInfo.getId());
            if(appGrids==null || appGrids.isEmpty()) {
                return false;
            }
            gridId = appGrids.get(0).getGridId();
        }
        String flowJson = appRuleUpdateRequest.getFlowJson();
        String description = appRuleUpdateRequest.getDescription();
        if(flowJson!=null&&!flowJson.isBlank()) {
            // 更新 flowJson 和 ruleJson
            if(!flowJson.equals(appRuleInfo.getFlowJson())) {
                // 转换JSON
                String jsonRule = convertNodeRedFlowJsonToAppRuleJson(flowJson, gridId);
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
        appRuleInfo.setAppName(appRuleSaveRequest.getAppName());
        appRuleInfo.setDescription(appRuleSaveRequest.getDescription());
        appRuleInfo.setRuleJson(appRuleSaveRequest.getRuleJson());
        appRuleInfo.setFlowJson(appRuleSaveRequest.getFlowJson());
        appRuleInfo.setUpdateTime(LocalDateTime.now());
        return appRuleInfo;
    }

    public PageDTO<AppRuleInfo> list(Integer projectId, AppRuleQueryRequest appRuleQueryRequest) {
        // 1. 动态创建 Sort 对象
        Sort sort;
        if (appRuleQueryRequest.getSortField() != null && !appRuleQueryRequest.getSortField().isEmpty()) {
            // 映射排序方向
            Sort.Direction direction = Sort.Direction.ASC;
            if ("descend".equals(appRuleQueryRequest.getSortOrder())) {
                direction = Sort.Direction.DESC;
            }
            sort = Sort.by(direction, appRuleQueryRequest.getSortField());
        } else {
            // 如果没有排序字段，默认按 id 升序
            sort = Sort.by("id").ascending();
        }
        // 2. 使用动态创建的 sort 对象
        Pageable pageable = PageRequest.of(
                appRuleQueryRequest.getPageNo() - 1,
                appRuleQueryRequest.getPageSize(),
                sort // 传入动态排序对象
        );
        // 3. 执行查询
        Page<AppRuleInfo> repoResult = appRuleRepository.searchByProjectWithFilters(
                projectId,
                appRuleQueryRequest.getEventType(),
                appRuleQueryRequest.getDescription(),
                pageable
        );
        // 4. 返回结果
        return new PageDTO<>(
                appRuleQueryRequest.getPageNo(), appRuleQueryRequest.getPageSize(),
                repoResult.getTotalElements(), repoResult.getTotalPages(),
                repoResult.getContent()
        );
    }

    public boolean updateEnabledStatus(Integer id, Boolean enabled) {
        AppGrid appGrid = appGridRepository.findById(id).orElse(null);
        if(appGrid==null) {
            return false;
        }
        appGrid.setEnabled(enabled);
        appGridRepository.save(appGrid);
        // TODO 需要下发边缘端
        return true;
    }

    public ResponseEntity<String> generateNaturalRule(RuleGenerateRequest ruleGenerateRequest) {
        String uuid = ruleGenerateRequest.getUuid();
        String message = ruleGenerateRequest.getMessage();
        String gridId = ruleGenerateRequest.getGridId();
        // 更新 uuid 的对话时间
        uuidUpdateTimeMap.put(uuid, System.currentTimeMillis());
        // 获取对话历史
        List<ChatMessage> messages = uuidMessageHistoryMap.getOrDefault(uuid, new ArrayList<>());
        // 如果是第一次对话，构造系统消息
        if(messages.isEmpty()) {
            // 根据网格ID获取环境级事件、属性、服务列表
            List<String> envEvents = envEventService.getEnvEventJsonList(gridId);
            List<String> envProperties = envPropertyService.getEnvPropertyStringList();
            List<String> envServices = envServiceService.getEnvServiceJsonList(gridId);
            String envEventsStr = String.join("\n", envEvents);
            String envPropertiesStr = String.join("\n", envProperties);
            String envServicesStr = String.join("\n", envServices);
            String systemPrompt = String.format(SystemPrompt.NATURAL_RULE_GENERATE_PROMPT, envEventsStr, envPropertiesStr, envServicesStr);
            System.out.println(systemPrompt);
            messages.add(new SystemMessage(systemPrompt));
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
        String gridId = ruleGenerateRequest.getGridId();
        // 构造系统消息和用户消息
        List<ChatMessage> messages = new ArrayList<>();
        // 构造系统提示词
        // 根据网格ID获取环境级事件、属性、服务列表
        List<String> envEvents = envEventService.getEnvEventJsonList(gridId);
        List<String> envProperties = envPropertyService.getEnvPropertyStringList();
        List<String> envServices = envServiceService.getEnvServiceJsonList(gridId);
        String envEventsStr = String.join("\n", envEvents);
        String envPropertiesStr = String.join("\n", envProperties);
        String envServicesStr = String.join("\n", envServices);
        String systemPrompt = String.format(SystemPrompt.JSON_RULE_GENERATE_PROMPT, envEventsStr, envPropertiesStr, envServicesStr);
        System.out.println(systemPrompt);
        messages.add(new SystemMessage(systemPrompt));
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
    private String convertNodeRedFlowJsonToAppRuleJson(String nodeRedFlowJson, String gridId) {
        // 构造系统消息和用户消息
        List<ChatMessage> messages = new ArrayList<>();
        // 根据网格ID获取环境级事件、属性、服务列表
        List<String> envEvents = envEventService.getEnvEventJsonList(gridId);
        List<String> envProperties = envPropertyService.getEnvPropertyStringList();
        List<String> envServices = envServiceService.getEnvServiceJsonList(gridId);
        String envEventsStr = String.join("\n", envEvents);
        String envPropertiesStr = String.join("\n", envProperties);
        String envServicesStr = String.join("\n", envServices);
        String systemPrompt = String.format(SystemPrompt.NODE_RED_CONVERT_JSON_RULE_PROMPT, envEventsStr, envPropertiesStr, envServicesStr);
        messages.add(new SystemMessage(systemPrompt));
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

    /**
     * 同步应用规则
     * */
    public List<AppRuleSyncResponse> syncAppRule(AppRuleSyncRequest appRuleSyncRequest) {
        Integer appId = appRuleSyncRequest.getAppId();
        List<String> gridIdList = appRuleSyncRequest.getGridIdList();
        // 获取应用规则
        AppRuleInfo appRuleInfo = appRuleRepository.findById(appId).orElse(null);
        if (appRuleInfo == null) {
            throw new BadRequestException("400", "应用规则不存在",  null);
        }
        // 解析应用规则，提取需要的环境级事件和服务
        AppRule appRule = parseJsonRule(appRuleInfo.getRuleJson());
        if(appRule == null) {
            throw new BadRequestException("400", "应用规则解析失败",  null);
        }
        String envEvent = appRule.getTrigger().getEvent_type();
        // 使用多线程并行检查每个 gridId 是否支持 envEvent 和 各自需要的环境级服务
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(gridIdList.size(), 5));
        List<Future<AppRuleSyncResponse>> futures = new ArrayList<>();
        for(String gridId : gridIdList) {
            Callable<AppRuleSyncResponse> task = () -> {
                try {
                    return checkGridIdSupport(gridId, appId, envEvent, appRule);
                } catch (Exception e) {
                    return new AppRuleSyncResponse(gridId, null, null, 0, e.getMessage());
                }
            };
            futures.add(executor.submit(task));
        }
        // 收集所有线程结果
        List<AppRuleSyncResponse> responses = new ArrayList<>();
        for (Future<AppRuleSyncResponse> future : futures) {
            try {
                responses.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                responses.add(new AppRuleSyncResponse(null, null, null, 0, e.getMessage()));
            }
        }
        // 关闭线程池
        executor.shutdown();
        return responses;
    }


    private void extractFromChain(List<ChainStep> chain, Set<String> envServiceSet, String gridId) {
        if (chain == null || chain.isEmpty()) {
            return;
        }
        for (ChainStep step : chain) {
            if (step instanceof ActionStep actionStep) {
                if (actionStep.getAction() != null && actionStep.getAction().getAction_name() != null) {
                    envServiceSet.add(actionStep.getAction().getAction_name());
                }
            } else if (step instanceof BranchStep branchStep) {
                if (branchStep.getBranch() != null) {
                    for (BranchNode branchNode : branchStep.getBranch()) {
                        if(isBranchConditionMet(branchNode, gridId)) {
                            extractFromChain(branchNode.getChain(), envServiceSet, gridId);
                        }
                    }
                }
            }  // WaitStep 不包含 action_name，跳过
        }
    }

    //检查环境级事件和服务是否支持
    private AppRuleSyncResponse checkGridIdSupport(String gridId, Integer appId, String envEvent, AppRule appRule) {
        GridMesh gridMesh = gridMeshRepository.findById(gridId).orElse(null);
        if(gridMesh == null) {
            return new AppRuleSyncResponse(gridId, null, null, 0, "网格不存在");
        }
        // 判断是否已经存在
        AppGrid oldAppGrid = appGridRepository.findByAppRuleIdAndGridId(appId, gridId);
        if (oldAppGrid != null) {
            return new AppRuleSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 1, "该网格已部署本应用");
        }
        // 动态获取当前网格实际需要支持的环境级服务
        Set<String> envServiceSet = extractEnvServiceByGrid(appRule, gridId);
        List<String> envEventTypeList = envEventService.getEnvEventTypeList(gridId);
        List<String> envServiceNameList = envServiceService.getEnvServiceNameList(gridId);
        // 检查 envEvent 是否在 envEventTypeList 中
        if (!envEventTypeList.contains(envEvent)) {
            return new AppRuleSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 0, "不支持环境级事件："+envEvent);
        }
        // 检查 envServiceSet 中的所有元素是否都在 envServiceNameList 中
        for (String envService : envServiceSet) {
            if (!envServiceNameList.contains(envService)) {
                return new AppRuleSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 0, "不支持环境级服务：" + envService);
            }
        }
        // 保存到数据库
        AppGrid appGrid = new AppGrid();
        appGrid.setAppRuleId(appId);
        appGrid.setGridId(gridId);
        appGrid.setEnabled(true);
        appGrid = appGridRepository.save(appGrid);
        if(appGrid.getId()==null) {
            return new AppRuleSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 0, "保存到数据库失败");
        }
        // TODO 下发到边端服务器
        // 所有检查通过
        return new AppRuleSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 1, "同步下发成功");
    }

    // 获取当前网格实际需要支持的环境级服务
    private Set<String> extractEnvServiceByGrid(AppRule appRule, String gridId) {
        Set<String> envServiceSet = new HashSet<>();
        if (appRule == null || appRule.getResponse() == null) {
            return envServiceSet;
        }
        Response response = appRule.getResponse();
        if(response.isChainType()) {
            extractFromChain(response.getChain(), envServiceSet, gridId);
        } else if(response.isBranchType()) {
            for(BranchNode branchNode : response.getBranch()) {
                if(isBranchConditionMet(branchNode, gridId)) {
                    extractFromChain(branchNode.getChain(), envServiceSet, gridId);
                }
            }
        }
        return envServiceSet;
    }

    // 判断分支条件是否满足
    private boolean isBranchConditionMet(BranchNode branchNode, String gridId) {
        if(branchNode.isHistoryCondition()) {
            return true;
        }
        CurrentCondition cond = branchNode.getCurrent_condition();
        if(cond.getCurrent_left()!=null&&"property".equals(cond.getCurrent_left().getType())) {
            String property = cond.getCurrent_left().getProperty();
            String productId = property.endsWith("_num") ? property.substring(0, property.lastIndexOf("_num")) : property;
            long leftVal = tslDeviceRepository.countByProductAndMesh(productId, gridId);
            try {
                long rightVal = Long.parseLong(cond.getRight());
                String operator = cond.getOperator();
                return evaluateCondition(leftVal, operator, rightVal);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    // 通用的条件求值器
    private boolean evaluateCondition(long left, String op, long right) {
        return switch (op) {
            case ">" -> left > right;
            case ">=" -> left >= right;
            case "<" -> left < right;
            case "<=" -> left <= right;
            case "==", "=" -> left == right;
            case "!=" -> left != right;
            default -> false;
        };
    }

    /**
     * 获取应用执行详情
     * */
    public List<AppRuleExecuteDetail> getAppRuleExecuteDetail(Integer appId) {
        List<AppGrid> appGridList = appGridRepository.findByAppRuleId(appId);
        return appGridList.stream().map(appGrid -> {
            AppRuleExecuteDetail appRuleExecuteDetail = new AppRuleExecuteDetail();
            appRuleExecuteDetail.setId(appGrid.getId());
            appRuleExecuteDetail.setGridId(appGrid.getGridId());
            appRuleExecuteDetail.setEnabled(appGrid.getEnabled());
            GridMesh gridMesh = gridMeshRepository.findById(appGrid.getGridId()).orElse(null);
            if(gridMesh != null) {
                appRuleExecuteDetail.setMeshNo(gridMesh.getMeshNo());
                appRuleExecuteDetail.setMeshName(gridMesh.getMeshName());
            }
            return appRuleExecuteDetail;
        }).collect(Collectors.toList());
    }
    /**
     * 根据网格ID获取应用规则列表
     */
    public List<AppRuleInfo> getAppRulesByGridId(String gridId) {
        return appRuleRepository.findByGridId(gridId);
    }
}
