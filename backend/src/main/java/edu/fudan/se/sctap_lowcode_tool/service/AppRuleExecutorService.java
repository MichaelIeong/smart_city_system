package edu.fudan.se.sctap_lowcode_tool.service;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.APPRULE.*;
import edu.fudan.se.sctap_lowcode_tool.DTO.AppRuleCompleteRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.EventTriggerRequest;
import edu.fudan.se.sctap_lowcode_tool.constant.CommandConstant;
import edu.fudan.se.sctap_lowcode_tool.constant.LogConstant;
import edu.fudan.se.sctap_lowcode_tool.constant.RedisConstant;
import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.model.AppRuleLog;
import edu.fudan.se.sctap_lowcode_tool.model.EventHistory;
import edu.fudan.se.sctap_lowcode_tool.repository.AppRuleLogRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.AppRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EventHistoryRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.redis.RedisUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AppRuleExecutorService {

    @Resource
    private EventHistoryRepository eventHistoryRepository;

    @Resource
    private AppRuleRepository appRuleRepository;

    @Resource
    private EventHistoryService eventHistoryService;

    @Resource
    private AppRuleLogRepository appRuleLogRepository;

    @Resource
    private WebSocketPushService webSocketPushService;

    @Resource
    private RedisUtil redisUtil;

    @Resource(name = "appRuleExecutor")
    private Executor appRuleExecutor;

    @Value("${tsl.app.id}")
    private String appId;

    @Value("${tsl.app.code}")
    private String appCode;

    @Value("${tsl.app.url}")
    private String appUrl;

    @Value("${tsl.app.token}")
    private String appToken;

    // 记录执行中的应用规则的日志
    @Getter
    Map<String, Map<String, List<String>>> appRuleLogMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initMockData() {
        log.info("✅ 初始化模拟应用规则数据...");

        String eventType = "ill_parking";
        Map<String, List<String>> meshMap = new ConcurrentHashMap<>();
        Set<String> locations = new HashSet<>();

        for (int i = 1; i <= 10; i++) {
            String location = String.format("%08d", 2*i);
            locations.add(location);
            List<String> logs = new ArrayList<>();

            logs.add("应用开始执行...");
            logs.add("检测到车辆违章停车");
            logs.add("AI 识别车牌号：沪A" + (1000 + i));
            logs.add("推送至交通管理部门处理中...");
            logs.add("等待人工确认...");

            meshMap.put(location, logs);
        }

        appRuleLogMap.put(eventType, meshMap);
        appRuleWaitMap.put(eventType, locations);

        log.info("✅ 模拟数据已加入 appRuleLogMap，共 {} 个事件类型，{} 条位置记录",
                appRuleLogMap.size(), meshMap.size());
    }


    // 记录处于等待中的应用规则
    Map<String, Set<String>> appRuleWaitMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 事件上报入口
     * */
    public void triggerAppRule(EventTriggerRequest eventTriggerRequest) {
        // 获取全部该事件类型的应用规则
        Integer projectId = eventTriggerRequest.getProjectId();
        String eventType = eventTriggerRequest.getEventType();
        List<AppRuleInfo> appRules = appRuleRepository.findByEventTypeAndProjectId(eventType, projectId);
        // 提交线程池执行
        for (AppRuleInfo appRuleInfo : appRules) {
            appRuleExecutor.execute(() -> executeAppRule(eventTriggerRequest, appRuleInfo));
        }
    }

    /**
     * 应用执行逻辑
     * */
    public void executeAppRule(EventTriggerRequest eventTriggerRequest, AppRuleInfo appRuleInfo) {
        // 解析JSON规则
        AppRule appRule = parseJsonRule(appRuleInfo.getRuleJson());
        // 提取事件类型和事件参数
        String eventType = eventTriggerRequest.getEventType();
        Map<String, Object> eventParams = extractEventParams(appRule, eventTriggerRequest.getParams());
        String waitKey = extractWaitKey(appRule);
        String waitValue = eventParams.get(waitKey).toString();
        // 判断应用是否处于等待中，如果不是则继续执行
        if(isAppRuleWaiting(eventType, waitValue)) {
            return;
        }
        // 增加开始执行日志
        addLog(LogConstant.INFO, eventType, waitValue, "应用开始执行...");
        // 向前端推送应用开始消息
        webSocketPushService.sendAlert(eventType, waitValue, CommandConstant.COMMAND_START);
        // 将事件加入数据库历史事件中
        storeEventHistory(eventType, eventParams, waitValue);
        // 处理response
        Response response = appRule.getResponse();
        handleResponse(response, eventType, eventParams, waitValue);
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
     * 提取事件参数
     * */
    private Map<String, Object> extractEventParams(AppRule appRule, Map<String, String> params) {
        Map<String, Object> eventParams = new HashMap<>();
        for(Map.Entry<String, String> entry: appRule.getTrigger().getEvent_params().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if(params.containsKey(key)) {
                switch (value) {
                    case "string":
                        eventParams.put(key, params.get(key));
                        break;
                    case "number":
                        eventParams.put(key, Integer.parseInt(params.get(key)));
                        break;
                    case "bool":
                        eventParams.put(key, Boolean.parseBoolean(params.get(key)));
                        break;
                    default:
                        eventParams.put(key, params.get(key));
                        log.warn("未知类型：{}", value);
                }
            }
        }
        return eventParams;
    }

    /**
     * 提取wait key
     * */
    private String extractWaitKey(AppRule appRule) {
        Response response = appRule.getResponse();
        List<ChainStep> chain = null;
        if(response.isChainType()) {
            chain = response.getChain();
        }
        if(response.isBranchType()) {
            chain = response.getBranch().get(0).getChain();
        }
        while(!chain.get(chain.size()-1).getType().equals("wait")) {
            BranchStep branchStep = (BranchStep) chain.get(chain.size()-1);
            chain = branchStep.getBranch().get(0).getChain();
        }
        WaitStep waitStep = (WaitStep) chain.get(chain.size()-1);
        return waitStep.getWait().getWaitKey();
    }

    /**
     * 判断应用是否处于等待中
     * */
    private boolean isAppRuleWaiting(String eventType, String waitValue) {
        Set<String> waitSet = appRuleWaitMap.get(eventType);
        if(waitSet!=null&&!waitSet.isEmpty()) {
            if(waitSet.contains(waitValue)) {
                log.info("{}-{}: 应用处于等待中...", eventType, waitValue);
                return true;
            }
        }
        return false;
    }

    /**
     * 增加日志
     * */
    private void addLog(String level, String eventType, String waitValue, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        String logMessage = String.format("[%s]-[%s]-[%s]-[%s]: %s", level, timestamp, eventType, waitValue, message);
        // 根据 level 调用不同的 log 方法
        switch (level) {
            case "ERROR":
                log.error(logMessage);
                break;
            case "WARN":
                log.warn(logMessage);
                break;
            case "INFO":
                log.info(logMessage);
                break;
            case "DEBUG":
                log.debug(logMessage);
                break;
            default:
                log.info(logMessage); // 默认 INFO
                break;
        }
        // 存储日志到内存 map
        appRuleLogMap
                .computeIfAbsent(eventType, k -> new HashMap<>())
                .computeIfAbsent(waitValue, k -> new ArrayList<>())
                .add(logMessage);
    }

    /**
     * 存储历史事件
     * */
    public void storeEventHistory(String eventType, Map<String, Object> eventParams, String waitValue) {
        addLog(LogConstant.INFO, eventType, waitValue, "开始存储历史事件...");
        try {
            EventHistory eventHistory = new EventHistory();
            eventHistory.setEventType(eventType);
            eventHistory.setLocation(eventParams.get("location").toString());
            eventHistory.setEventData(objectMapper.writeValueAsString(eventParams));
            eventHistory.setTimestamp(LocalDateTime.now());
            eventHistoryService.saveEventHistory(eventHistory);
            addLog(LogConstant.INFO, eventType, waitValue, "存储历史事件成功...");
        } catch (Exception e) {
            addLog(LogConstant.ERROR, eventType, waitValue, "存储历史事件失败: " + e.getMessage());
        }
    }

    /**
     * 处理 response
     * */
    private void handleResponse(Response response, String eventType, Map<String, Object> eventParams, String waitValue) {
        if(response.isBranchType()) {
            // 处理branch
            List<BranchNode> branchNodes = response.getBranch();
            BranchStep branchStep = new BranchStep();
            branchStep.setBranch(branchNodes);
            handleBranchStep(branchStep, eventType, eventParams, waitValue);
            return;
        }
        if(response.isChainType()) {
            // 处理chain
            List<ChainStep> chain = response.getChain();
            handleChain(chain, eventType, eventParams, waitValue);
        }
    }

    /**
     * 处理 branchStep
     * */
    private void handleBranchStep(BranchStep branchStep, String eventType, Map<String, Object> eventParams, String waitValue) {
        addLog(LogConstant.INFO, eventType, waitValue, "开始处理分支条件逻辑...");
        List<BranchNode> branchNodes = branchStep.getBranch();
        for(BranchNode branchNode : branchNodes) {
            if(branchNode.isCurrentCondition()) {
                // 处理currentCondition
                CurrentCondition currentCondition = branchNode.getCurrent_condition();
                if(checkCurrentCondition(currentCondition, eventType, eventParams, waitValue)) {
                    handleChain(branchNode.getChain(), eventType, eventParams, waitValue);
                    // 命中一个条件就不再检查其他条件
                    return;
                }
            }
            if(branchNode.isHistoryCondition()) {
                // 处理historyCondition
                HistoryCondition historyCondition = branchNode.getHistory_condition();
                if(checkHistoryCondition(historyCondition, eventType, eventParams, waitValue)) {
                    handleChain(branchNode.getChain(), eventType, eventParams, waitValue);
                    // 命中一个条件就不再检查其他条件
                    return;
                }
            }
        }
    }

    /**
     * 检查 current_condition
     * */
    private boolean checkCurrentCondition(CurrentCondition currentCondition, String eventType, Map<String, Object> eventParams, String waitValue) {
        addLog(LogConstant.INFO, eventType, waitValue, "开始检查当前条件...");
        CurrentCondition.CurrentLeft currentLeft = currentCondition.getCurrent_left();
        String operator = currentCondition.getOperator();
        String right = currentCondition.getRight();
        switch (currentLeft.getType()) {
            case "time":
                return checkCurrentTimeCondition(operator, right, eventType, waitValue);
            case "location":
                return checkCurrentLocation(operator, right, eventParams, eventType, waitValue);
            case "property":
                return checkCurrentProperty(currentLeft.getProperty(), operator, right, eventParams, eventType, waitValue);
            default:
                addLog(LogConstant.ERROR, eventType, waitValue, String.format("当前条件检查失败, 不支持的类型: '%s'", currentLeft.getType()));
                return false;
        }
    }

    /**
     * 检查当前时间条件
     * */
    private boolean checkCurrentTimeCondition(String operator, String right, String eventType, String waitValue) {
        addLog(LogConstant.INFO, eventType, waitValue, String.format("开始检查当前时间条件, operator: '%s', right: '%s'", operator, right));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        LocalTime now = LocalTime.now();
        try {
            LocalTime conditionTime = LocalTime.parse(right);
            String nowStr = now.format(timeFormatter);
            String condStr = conditionTime.format(timeFormatter);
            boolean result;
            String detail; // 详细比较说明
            switch (operator) {
                case ">":
                case ">=":
                    result = now.isAfter(conditionTime);
                    detail = String.format("当前时间 '%s' %s '%s'", nowStr,
                            result ? "晚于" : "不晚于", condStr);
                    break;
                case "<":
                case "<=":
                    result = now.isBefore(conditionTime);
                    detail = String.format("当前时间 '%s' %s '%s'", nowStr,
                            result ? "早于" : "不早于", condStr);
                    break;
                case "=":
                case "==":
                    result = now.equals(conditionTime);
                    detail = String.format("当前时间 '%s' %s '%s'", nowStr,
                            result ? "等于" : "不等于", condStr);
                    break;
                case "!=":
                    result = !now.equals(conditionTime);
                    detail = String.format("当前时间 '%s' %s '%s'", nowStr,
                            result ? "不等于" : "等于", condStr);
                    break;
                default:
                    addLog(LogConstant.ERROR, eventType, waitValue, String.format("当前时间条件检查失败, 不支持的运算符: '%s'", operator));
                    return false;
            }
            if (result) {
                addLog(LogConstant.INFO, eventType, waitValue, detail + ", 条件成立。");
                return true;
            } else {
                addLog(LogConstant.INFO, eventType, waitValue, detail + ", 条件不成立。");
                return false;
            }
        } catch (Exception e) {
            addLog(LogConstant.ERROR, eventType, waitValue, "当前时间条件检查失败:  " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查当前位置
     * */
    private boolean checkCurrentLocation(String operator, String right, Map<String, Object> eventParams, String eventType, String waitValue) {
        addLog(LogConstant.INFO, eventType, waitValue, String.format("开始检查当前位置条件, operator: '%s', right: '%s'", operator, right));
        String location = (String) eventParams.get("location");
        if (location == null) {
            addLog(LogConstant.ERROR, eventType, waitValue, "当前位置条件检查失败, 上报事件中不存在 'location' 字段");
            return false;
        }
        boolean result;
        String detail;
        switch (operator) {
            case "=":
            case "==":
                result = location.equals(right);
                detail = String.format("当前位置 '%s' %s 条件位置 '%s'", location, result ? "等于" : "不等于", right);
                break;
            case "!=":
                result = !location.equals(right);
                detail = String.format("当前位置 '%s' %s 条件位置 '%s'", location, result ? "不等于" : "等于", right);
                break;
            default:
                addLog(LogConstant.ERROR, eventType, waitValue, String.format("当前位置条件检查失败, 不支持的运算符: '%s', 仅支持 '==' 和 '!='", operator));
                return false;
        }
        if (result) {
            addLog(LogConstant.INFO, eventType, waitValue, detail + "，条件成立。");
        } else {
            addLog(LogConstant.INFO, eventType, waitValue, detail + "，条件不成立。");
        }
        return result;
    }

    /**
     * 检查当前属性
     * */
    private boolean checkCurrentProperty(String property, String operator, String right, Map<String, Object> eventParams, String eventType, String waitValue) {
        addLog(LogConstant.INFO, eventType, waitValue, String.format("开始检查当前属性条件, property: '%s', operator: '%s', right: '%s'", property, operator, right));
        if (property == null || property.isEmpty()) {
            addLog(LogConstant.ERROR, eventType, waitValue, "当前属性条件检查失败，'property' 参数为空");
            return false;
        }
        String location = (String) eventParams.get("location");
        if (location == null) {
            addLog(LogConstant.ERROR, eventType, waitValue, "当前属性条件检查失败，上报的事件中不存在 'location'");
            return false;
        }
        int leftVal;
        int rightVal;
        try {
            // TODO: 从数据库获取属性值，这里先默认属性值为1
            leftVal = 1;
            rightVal = Integer.parseInt(right);
        } catch (NumberFormatException e) {
            addLog(LogConstant.ERROR, eventType, waitValue, String.format("当前属性条件检查失败, right: '%s' 不是有效的整数", right));
            return false;
        }
        boolean result;
        String detail;
        switch (operator) {
            case "=":
            case "==":
                result = leftVal == rightVal;
                detail = String.format("属性[%s] 当前值 %d %s 条件值 %d", property, leftVal, result ? "等于" : "不等于", rightVal);
                break;
            case ">":
                result = leftVal > rightVal;
                detail = String.format("属性[%s] 当前值 %d %s 条件值 %d", property, leftVal, result ? "大于" : "不大于", rightVal);
                break;
            case ">=":
                result = leftVal >= rightVal;
                detail = String.format("属性[%s] 当前值 %d %s 条件值 %d", property, leftVal, result ? "大于等于" : "小于", rightVal);
                break;
            case "<":
                result = leftVal < rightVal;
                detail = String.format("属性[%s] 当前值 %d %s 条件值 %d", property, leftVal, result ? "小于" : "不小于", rightVal);
                break;
            case "<=":
                result = leftVal <= rightVal;
                detail = String.format("属性[%s] 当前值 %d %s 条件值 %d", property, leftVal, result ? "小于等于" : "大于", rightVal);
                break;
            case "!=":
                result = leftVal != rightVal;
                detail = String.format("属性[%s] 当前值 %d %s 条件值 %d", property, leftVal, result ? "不等于" : "等于", rightVal);
                break;
            default:
                addLog(LogConstant.ERROR, eventType, waitValue, String.format("当前属性条件检查失败, 不支持的运算符: '%s', 支持：'==', '>', '>=', '<', '<=', '!='", operator));
                return false;
        }
        if (result) {
            addLog(LogConstant.INFO, eventType, waitValue, detail + ", 条件成立");
        } else {
            addLog(LogConstant.INFO, eventType, waitValue, detail + ", 条件不成立");
        }
        return result;
    }

    /**
     * 检查 history_condition
     * */
    private boolean checkHistoryCondition(HistoryCondition historyCondition, String eventType, Map<String, Object> eventParams, String waitValue) {
        addLog(LogConstant.INFO, eventType, waitValue, "开始检查历史条件...");
        HistoryCondition.HistoryLeft historyLeft = historyCondition.getHistory_left();
        String operator = historyCondition.getOperator();
        String right = historyCondition.getRight();
        String func = historyLeft.getFunc();
        Map<String, String> funcParams = historyLeft.getFunc_params();
        String funcKey = funcParams.entrySet().iterator().next().getKey();
        // 校验func的格式
        String regex = "(\\w+)\\(([^)]+)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(func);
        if(matcher.find()) {
            String functionName = matcher.group(1);
            String paramStr = matcher.group(2);
            String[] paramArray = paramStr.split(",\\s*");
            if(paramArray.length != 3) {
                addLog(LogConstant.ERROR, eventType, waitValue, "历史条件检查失败, func参数错误: "+ func);
                return false;
            }
            String duration = paramArray[1];
            String unit = paramArray[2];
            int durationVal = Integer.parseInt(duration);
            // 计算历史值
            int leftVal = eventCount(eventType, durationVal, unit, funcKey, eventParams, waitValue);
            int rightVal = Integer.parseInt(right);
            boolean result;
            String detail;
            switch (operator) {
                case "=":
                case "==":
                    result = leftVal == rightVal;
                    detail = String.format("历史事件值 %d %s 条件值 %d", leftVal, result ? "等于" : "不等于", rightVal);
                    break;
                case ">":
                    result = leftVal > rightVal;
                    detail = String.format("历史事件值 %d %s 条件值 %d", leftVal, result ? "大于" : "不大于", rightVal);
                    break;
                case ">=":
                    result = leftVal >= rightVal;
                    detail = String.format("历史事件值 %d %s 条件值 %d", leftVal, result ? "大于等于" : "小于", rightVal);
                    break;
                case "<":
                    result = leftVal < rightVal;
                    detail = String.format("历史事件值 %d %s 条件值 %d", leftVal, result ? "小于" : "不小于", rightVal);
                    break;
                case "<=":
                    result = leftVal <= rightVal;
                    detail = String.format("历史事件值 %d %s 条件值 %d", leftVal, result ? "小于等于" : "大于", rightVal);
                    break;
                case "!=":
                    result = leftVal != rightVal;
                    detail = String.format("历史事件值 %d %s 条件值 %d", leftVal, result ? "不等于" : "等于", rightVal);
                    break;
                default:
                    addLog(LogConstant.ERROR, eventType, waitValue, String.format("当前属性条件检查失败, 不支持的运算符: '%s', 支持：'==', '>', '>=', '<', '<=', '!='", operator));
                    return false;
            }
            if (result) {
                addLog(LogConstant.INFO, eventType, waitValue, detail + ", 条件成立");
            } else {
                addLog(LogConstant.INFO, eventType, waitValue, detail + ", 条件不成立");
            }
            return result;
        } else {
            addLog(LogConstant.ERROR, eventType, waitValue, "历史条件检查失败, func格式错误: " + func);
            return false;
        }
    }

    /**
     * 计算某事件过去一段时间发生的次数
     */
    private int eventCount(String eventType, int duration, String unit, String funcKey, Map<String, Object> eventParams, String waitValue) {
        addLog(LogConstant.INFO, eventType, waitValue, String.format("开始计算历史事件次数, 事件类型: '%s', 持续时间: %d%s, 额外参数: '%s'", eventType, duration, unit, funcKey));
        LocalDateTime startTime;
        switch (unit) {
            case "second":
            case "seconds":
                startTime = LocalDateTime.now().minusSeconds(duration);
                break;
            case "minute":
            case "minutes":
                startTime = LocalDateTime.now().minusMinutes(duration);
                break;
            case "hour":
            case "hours":
                startTime = LocalDateTime.now().minusHours(duration);
                break;
            default:
                addLog(LogConstant.ERROR, eventType, waitValue, String.format("历史事件计算失败, 时间单位错误: '%s', 支持 'second', 'minute', 'hour'", unit));
                return 0;
        }
        List<EventHistory> eventHistories = eventHistoryRepository.findByEventTypeSince(eventType, startTime);
        int count = 0;
        if(funcKey==null) {
            count = eventHistories.size();
            addLog(LogConstant.INFO, eventType, waitValue, "历史事件次数计算结果为: " + count);
            return count;
        }
        String funcValue = eventParams.get(funcKey).toString();
        for(EventHistory eventHistory: eventHistories) {
            try {
                Map eventData = objectMapper.readValue(eventHistory.getEventData(), Map.class);
                if(funcValue.equals(eventData.get(funcKey).toString())) {
                    count++;
                }
            } catch (Exception e) {
                addLog(LogConstant.ERROR, eventType, waitValue, "历史事件计算失败, eventData解析失败: " + eventHistory.getEventData());
                return 0;
            }
        }
        addLog(LogConstant.INFO, eventType, waitValue, "历史事件次数计算结果为: " + count);
        return count;
    }

    /**
     * 处理 chain
     * */
    private void handleChain(List<ChainStep> chain, String eventType, Map<String, Object> eventParams, String waitValue) {
        addLog(LogConstant.INFO, eventType, waitValue, "开始处理顺序链路逻辑...");
        for(int i=0;i<chain.size();i++) {
            ChainStep step = chain.get(i);
            switch (step.getType()) {
                case "action" :
                    handleActionStep((ActionStep) step, eventType, eventParams, waitValue);
                    break;
                case "branch":
                    handleBranchStep((BranchStep) step, eventType, eventParams, waitValue);
                    break;
                case "wait":
                    handleWaitStep((WaitStep) step, eventType, eventParams, chain, i, waitValue);
                    return;
                default:
                    addLog(LogConstant.WARN, eventType, waitValue, "未知顺序链路步骤类型: " + step.getType());
            }
        }
    }

    /**
     * 处理 actionStep
     * */
    private void handleActionStep(ActionStep actionStep, String eventType, Map<String, Object> eventParams, String waitValue) {
        addLog(LogConstant.INFO, eventType, waitValue, "开始处理action...");
        // TODO ，这里暂时模拟执行动作
        ActionStep.Action action = actionStep.getAction();
        addLog(LogConstant.INFO, eventType, waitValue, String.format("下发执行动作: '%s', 事件类型: '%s', 地点: '%s'", action.getAction_name(), eventType, eventParams.get("location")));
    }

    /**
     * 处理 waitStep
     * */
    private void handleWaitStep(WaitStep waitStep, String eventType, Map<String, Object> eventParams, List<ChainStep> chain, int index, String waitValue) {
        addLog(LogConstant.INFO, eventType, waitValue, "开始处理wait...");
        // 将应用加入等待
        appRuleWaitMap
                .computeIfAbsent(eventType, k -> new HashSet<>())
                .add(waitValue);
        // wait是chain的最后一个步骤
        if(chain.size() != index + 1) {
            addLog(LogConstant.WARN, eventType, waitValue, "wait步骤不是chain的最后一个步骤, 请检查应用规则!");
        }
        Map<String, Object> redisData = new HashMap<>();
        redisData.put("eventType", eventType);
        redisData.put("waitValue", waitValue);
        long currentTimeMillis = System.currentTimeMillis();
        String redisKey = "";
        // 处理action_wait
        WaitStep.Wait wait = waitStep.getWait();
        if(wait.isActionWait()) {
            addLog(LogConstant.INFO, eventType, waitValue, String.format("事件 '%s' 加入动作等待, 标识: '%s'", eventType, waitValue));
            redisKey = RedisConstant.ActionWait + eventType + ":" + waitValue;
            // 这里设定 action_condition 的超时时间为 1 小时
            long expireTimeMillis = currentTimeMillis + 60 * 60 * 1000L;
            redisData.put("expireTime", expireTimeMillis);
        }
        // 处理time_wait
        if(wait.isTimeWait()) {
            int waitDuration = Integer.parseInt(wait.getTime_wait().getDuration());
            String waitUnit = wait.getTime_wait().getUnit();
            addLog(LogConstant.INFO, eventType, waitValue, String.format("事件 '%s' 加入时间等待, 等待时长: %d%s 标识: '%s'", eventType, waitDuration, waitUnit, waitValue));
            redisKey = RedisConstant.TimeWait + eventType + ":" + waitValue;
            // 存储到期时间
            long expireTimeMillis = currentTimeMillis;
            switch (waitUnit) {
                case "second":
                case "seconds":
                    expireTimeMillis += waitDuration * 1000L;
                    break;
                case "minute":
                case "minutes":
                    expireTimeMillis += waitDuration * 60 * 1000L;
                    break;
                case "hour":
                case "hours":
                    expireTimeMillis += waitDuration * 60 * 60 * 1000L;
                    break;
                default:
                    addLog(LogConstant.WARN, eventType, waitValue, "时间单位错误: '" + waitUnit + "', 支持 'second', 'minute', 'hour'");
                    // 默认使用分钟
                    expireTimeMillis += waitDuration * 60 * 1000L;
            }
            redisData.put("expireTime", expireTimeMillis);
        }
        // 存储到 redis
        try {
            redisUtil.setWait(redisKey, redisData);
        } catch (JsonProcessingException e) {
            addLog(LogConstant.ERROR, eventType, waitValue, "wait步骤处理失败, redis存储失败");
        }
    }

    /**
     * 动作上报完成
     * */
    public void complete(AppRuleCompleteRequest appRuleCompleteRequest) {
        String eventType = appRuleCompleteRequest.getEventType();
        String waitValue = appRuleCompleteRequest.getValue();
        String redisKey = RedisConstant.ActionWait + eventType + ":" + waitValue;
        // 从 redis 中删除
        redisUtil.deleteSingle(redisKey);
        // 从等待中移除
        Set<String> waitSet = appRuleWaitMap.get(eventType);
        waitSet.remove(waitValue);
        appRuleWaitMap.put(eventType, waitSet);
        addLog(LogConstant.INFO, eventType, waitValue, String.format("事件 '%s' 结束动作等待, 标识: '%s'", eventType, waitValue));
        addLog(LogConstant.INFO, eventType, waitValue, "应用流程执行结束...");
        // 向前端推送应用结束消息
        webSocketPushService.sendAlert(eventType, waitValue, CommandConstant.COMMAND_END);
        // 将日志存入数据库
        saveLog(eventType, waitValue);
    }

    /**
     * 将日志存入数据库
     */
    public void saveLog(String eventType, String waitValue) {
        try {
            AppRuleLog appRuleLog = new AppRuleLog();
            appRuleLog.setEventType(eventType);
            appRuleLog.setWaitValue(waitValue);
            // 从 appRuleLogMap 中获取
            List<String> logs = appRuleLogMap.get(eventType).get(waitValue);
            appRuleLog.setLogs(objectMapper.writeValueAsString(logs));
            appRuleLog.setTimestamp(LocalDateTime.now());
            appRuleLogRepository.save(appRuleLog);
            // 删除 appRuleLogMap 中
            appRuleLogMap.get(eventType).remove(waitValue);
        } catch (JsonProcessingException e) {
            log.error("日志转换失败{}", e.getMessage());
        }
    }

    /**
     * 获取正在运行的事件
     * */
    public List<Map<String, Object>> getRunningEvents() {
        List<Map<String, Object>> runningEvents = new ArrayList<>();
        for(String eventType : appRuleLogMap.keySet()) {
            Map<String, Object> runningEvent = new HashMap<>();
            runningEvent.put("eventType", eventType);
            runningEvent.put("instanceNum", appRuleLogMap.get(eventType).size());
            runningEvents.add(runningEvent);
        }
        return runningEvents;
    }

    /**
     * 获取某一事件的所有执行标识
     * */
    public List<String> getWaitValueOfEvent(String eventType) {
        if(appRuleLogMap.containsKey(eventType)) {
            return appRuleLogMap.get(eventType).keySet().stream().toList();
        }
        return new ArrayList<>();
    }

    /**
     * 获取日志
     * */
    public List<String> getLog(String eventType, String waitValue) {
        Map<String, List<String>> logMap = appRuleLogMap.get(eventType);
        if(logMap!=null) {
            if(logMap.containsKey(waitValue)) {
                return logMap.get(waitValue);
            }
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    /**
     * 定时任务：每隔30s执行一次，检查到期的TimeWait并执行
     * */
    public void checkExpiredTimeWait() {
        log.info("开始检查到期的时间等待应用...");
        // 获取所有以 timeCondition 前缀开头的 key 对应的值
        List<String> waits = redisUtil.getAll(RedisConstant.TimeWait);
        if(waits == null || waits.isEmpty()) {
            log.info("没有待检查的时间等待应用...");
            return;
        }
        long now = System.currentTimeMillis();
        for(String wait : waits) {
            try {
                if(wait.trim().isEmpty()) {
                    continue;
                }
                // 反序列化
                Map waitData = objectMapper.readValue(wait, Map.class);
                long expireTime = Long.parseLong(waitData.get("expireTime").toString());
                if(now >= expireTime) {
                    String eventType = waitData.get("eventType").toString();
                    String waitValue = waitData.get("waitValue").toString();
                    String redisKey = RedisConstant.TimeWait + eventType + ":" + waitValue;
                    // 从 redis 中删除
                    redisUtil.deleteSingle(redisKey);
                    // 从等待中移除
                    Set<String> waitSet = appRuleWaitMap.get(eventType);
                    waitSet.remove(waitValue);
                    appRuleWaitMap.put(eventType, waitSet);
                    addLog(LogConstant.INFO, eventType, waitValue, String.format("事件 '%s' 结束时间等待, 标识: '%s'", eventType, waitValue));
                    addLog(LogConstant.INFO, eventType, waitValue, "应用流程执行结束...");
                    // 向前端推送应用结束消息
                    webSocketPushService.sendAlert(eventType, waitValue, CommandConstant.COMMAND_END);
                    // 存储日志
                    saveLog(eventType, waitValue);
                }
            } catch (Exception e) {
                log.error("反序列化 wait 数据失败：{}", e.getMessage());
            }
        }
    }

    /**
     * 定时任务：每隔1小时执行一次，检查到期的TimeWait并执行
     * */
    public void checkExpiredActionWait() {
        log.info("开始检查到期的动作等待应用...");
        // 获取所有以 timeCondition 前缀开头的 key 对应的值
        List<String> waits = redisUtil.getAll(RedisConstant.ActionWait);
        if(waits == null || waits.isEmpty()) {
            log.info("没有待检查的动作等待应用...");
            return;
        }
        long now = System.currentTimeMillis();
        for(String wait : waits) {
            try {
                if(wait.trim().isEmpty()) {
                    continue;
                }
                // 反序列化
                Map waitData = objectMapper.readValue(wait, Map.class);
                long expireTime = Long.parseLong(waitData.get("expireTime").toString());
                if(now >= expireTime) {
                    String eventType = waitData.get("eventType").toString();
                    String waitValue = waitData.get("waitValue").toString();
                    String redisKey = RedisConstant.ActionWait + eventType + ":" + waitValue;
                    // 从 redis 中删除
                    redisUtil.deleteSingle(redisKey);
                    // 从等待中移除
                    Set<String> waitSet = appRuleWaitMap.get(eventType);
                    waitSet.remove(waitValue);
                    appRuleWaitMap.put(eventType, waitSet);
                    addLog(LogConstant.INFO, eventType, waitValue, String.format("事件 '%s' 结束动作等待, 标识: '%s'", eventType, waitValue));
                    addLog(LogConstant.INFO, eventType, waitValue, "应用流程执行结束...");
                    // 存储日志
                    saveLog(eventType, waitValue);
                }
            } catch (Exception e) {
                log.error("反序列化 wait 数据失败：{}", e.getMessage());
            }
        }
    }

    /**
     * 获取特斯联事件数据
     * */
    public List<Map<String, Object>> getTslEventData(int pageNum, int pageSize) {
        Map<String, String> bodyParam = new HashMap<>();
        bodyParam.put("status", "4");
        bodyParam.put("filterMerge", "2");
        bodyParam.put("pageNum", String.valueOf(pageNum));
        bodyParam.put("pageSize", String.valueOf(pageSize));

        String timestamp = System.currentTimeMillis() + "";
        String nonce = RandomUtil.randomInt(5) + "";
        String sign = MD5.create().digestHex(appId + appToken + timestamp + nonce, CharsetUtil.CHARSET_UTF_8);

        HttpRequest request = HttpUtil.createPost(appUrl)
                .header("Context-Type", "application/json")
                .header("appId", appId)
                .header("appCode", appCode)
                .header("nonce", nonce)
                .header("timestamp", timestamp)
                .header("sign", sign)
                .header("authorization", appToken)
                .body(JSON.toJSONString(bodyParam));
        try (HttpResponse response = request.execute()) {
            int statusCode = response.getStatus();
            String responseBody = response.body();
            if (statusCode == 200) {
                List<Map<String, Object>> eventDataList = parseEventData(responseBody);
                return eventDataList;
            }
            return null;
        } catch (Exception e) {
            log.error("获取特斯联事件数据失败：{}", e.getMessage());
            return null;
        }
    }

    private List<Map<String, Object>> parseEventData(String response) {
        // 解析 JSON 响应
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject data = jsonObject.getJSONObject("data"); // 获取 "data" 部分
        JSONArray datas = data.getJSONArray("datas"); // 获取 "datas" 数组
        // 使用 TypeReference 进行类型转换
        Type type = new com.alibaba.fastjson.TypeReference<List<Map<String, Object>>>() {}.getType();
        return JSON.parseObject(datas.toString(), type);
    }

    public EventTriggerRequest parseEventTriggerRequest(Map<String, Object> eventData) {
        EventTriggerRequest eventTriggerRequest = new EventTriggerRequest();
        // 这里先设定 projectId = 1
        eventTriggerRequest.setProjectId(1);
        // 获取事件类型和事件参数
        String eventType = eventData.get("eventType").toString();
        eventTriggerRequest.setEventType(eventType);
        Map<String, String> params = new HashMap<>();
        // params.put("location", eventData.get("address").toString());
        // TODO, 先暂时使用 00000060 作为测试
        params.put("location", "00000060");
        // 如果是井盖水浸或者井盖倾斜
        if(eventType.equals("manhole-flooding")||eventType.equals("manhole-tilte")) {
            Map<String, Object> words = (Map<String, Object>) eventData.get("words");
            params.put("deviceId", words.get("deviceId").toString());
        }
        // 如果是机动车违停占道或者渣土车识别
        if(eventType.equals("ill_parking")||eventType.equals("truck_dect")) {
            Map<String, Object> words = (Map<String, Object>) eventData.get("words");
            params.put("plate_number", words.get("plate_number").toString());
        }
        eventTriggerRequest.setParams(params);
        return eventTriggerRequest;
    }

    /**
     * 定时任务：每隔30s执行一次，获取特斯联数据然后执行应用应用规则
     * */
    public void getTslEventDataAndExecuteAppRule() {
        List<Map<String, Object>> eventDataList = getTslEventData(1, 10);
        // 遍历事件数据触发应用规则
        for(Map<String, Object> eventData : eventDataList) {
            EventTriggerRequest eventTriggerRequest = parseEventTriggerRequest(eventData);
            triggerAppRule(eventTriggerRequest);
        }
    }
}
