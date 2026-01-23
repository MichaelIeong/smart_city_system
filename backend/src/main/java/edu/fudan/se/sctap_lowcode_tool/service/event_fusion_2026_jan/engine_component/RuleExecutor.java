package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.Param;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.DataEvent;
import edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan.EventFusionRunHistory;
import edu.fudan.se.sctap_lowcode_tool.repository.EventFusionRunHistoryRepository;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.common_operator.CommonOperatorRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule.EventSource.spaceEvent;
import static edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule.OperatorType.common;
import static edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule.OperatorType.service;

/**
 * <h3>RuleExecutor 规则执行器</h3>
 * 负责执行匹配到的规则，并生成融合后的事件结果。
 *
 * @author Lin Yicheng
 * @since 2026-01-16
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuleExecutor {

    private final EventFusionRunHistoryRepository runHistoryRepository;
    private final CommonOperatorRegistry commonOperatorRegistry;

    /**
     * 执行规则
     *
     * @param rule     待执行的规则
     * @param triggers 触发该规则的事件列表
     * @return 执行结果事件，若该规则未生成融合结果则返回空 Optional
     */
    public Optional<DataEvent> execute(EventFusionRule rule, List<DataEvent> triggers) {
        RuleExecutionContext context = new RuleExecutionContext(rule, triggers);
        return context.execute();
    }

    private class RuleExecutionContext {
        /** 所属的事件融合规则 */
        private final EventFusionRule rule;
        /** 触发该规则的事件列表 */
        private final List<DataEvent> triggers;
        /** 各步骤的执行状态 key: 步骤ID value: 执行状态(待执行、执行中、已完成、失败) */
        private final Map<String, StepStatus> stepStatusMap = new HashMap<>();
        /** 全局数据变量(原始事件数据) 格式: #triggers[eventSource][eventId][key] */
        private final Map<String, Map<String, Map<String, Object>>> triggersInput = new HashMap<>();
        /** 全局数据变量(各步骤输出结果) 格式: #stepOutputs[stepId][key] */
        private final Map<String, Map<String, Object>> stepOutputs = new HashMap<>();
        /** 待发布的事件 */
        @Nullable private DataEvent publishEvent = null;
        /** 执行日志 */
        private final List<String> logs = new ArrayList<>();
        /** 步骤依赖关系图(即将next依赖转化为after依赖) key: 步骤ID value: 该步骤的所有前置步骤ID */
        private final Map<String, List<String>> dependencies;

        private enum StepStatus {PENDING, EXECUTING, COMPLETED, FAILED}
        private static class FlowAbortException extends Exception {}

        // ==========================================
        // 主要步骤(初始化、执行算子、发布、中止)
        // ==========================================

        /** 初始化运行上下文 */
        private RuleExecutionContext(EventFusionRule rule, List<DataEvent> triggers) {
            this.rule = rule;
            this.triggers = triggers;
            // 构建步骤依赖关系图
            this.dependencies = buildDependencies(rule.steps());
            // 初始化各步骤状态为 PENDING (待执行)
            for (EventFusionRule.Step step : rule.steps()) {
                this.stepStatusMap.put(step.stepId(), StepStatus.PENDING);
            }
            // 构建触发事件的输入数据
            for (DataEvent trigger : this.triggers) {
                String sourceKey = trigger.getEventSource().name();
                var byEventSource = triggersInput.computeIfAbsent(sourceKey, key -> new HashMap<>());
                byEventSource.put(trigger.getEventId(), trigger.getPayload());
            }
            log("Context(rule=" + rule.ruleName() + ")初始化完成");
        }

        /** 执行规则 */
        private Optional<DataEvent> execute() {
            try {
                while (true) {
                    var executableSteps = findExecutableSteps();
                    if (executableSteps.isEmpty()) break;
                    for (EventFusionRule.Step step : executableSteps) executeStep(step);
                }
                if (stepStatusMap.values().stream().anyMatch(step -> step != StepStatus.COMPLETED))
                    logAndAbort("根据依赖分析，当前没有可执行的步骤了。但仍存在未完成的步骤，规则执行终止。");
                return publish();
            } catch (FlowAbortException ex) {
                return abort();
            } catch (Exception e) {
                log("在规则执行过程中发生了未捕获的错误:(%s) %s".formatted(e.getClass().getSimpleName(), e.getMessage()));
                return abort();
            }
        }

        /** 执行单个步骤 */
        private void executeStep(EventFusionRule.Step step) throws FlowAbortException {
            String formattedStepId = formatStepId(step.stepId());
            stepStatusMap.put(step.stepId(), StepStatus.EXECUTING);
            log("开始执行节点: " + formattedStepId);

            // 评估步骤条件
            try {
                if (!evaluateCondition(step.condition())) {
                    stepStatusMap.put(step.stepId(), StepStatus.FAILED);
                    logAndAbort("节点" + formattedStepId + "条件判断为false，规则执行终止");
                }
            } catch (ExpressionEvaluationException ex) {
                stepStatusMap.put(step.stepId(), StepStatus.FAILED);
                logAndAbort("节点" + formattedStepId + "条件判断时发生异常：" + ex.getMessage() + "，规则执行终止");
            }

            try {
                // 对入参表达式求值
                Map<String, Object> inputArgs = evaluateParams(step.input());
                log("节点" + formattedStepId + "输入参数：" + inputArgs);
                // 调用算子执行逻辑
                Map<String, Object> resultMap = invokeOperator(step, inputArgs);
                // 记录算子输出
                stepOutputs.put(step.stepId(), resultMap);
                // 更新步骤状态
                stepStatusMap.put(step.stepId(), StepStatus.COMPLETED);
                log("节点" + formattedStepId + "执行完成，输出结果：" + resultMap);
            } catch (ExpressionEvaluationException ex) {
                stepStatusMap.put(step.stepId(), StepStatus.FAILED);
                logAndAbort("节点" + formattedStepId + "解析输入参数时发生异常：" + ex.getMessage() + "，规则执行终止");
            } catch (OperatorException ex) {
                stepStatusMap.put(step.stepId(), StepStatus.FAILED);
                logAndAbort("节点" + formattedStepId + "执行算子时发生异常：" + ex.getMessage() + "，规则执行终止");
            }

        }

        /** 发布融合事件 */
        private Optional<DataEvent> publish() throws FlowAbortException {
            EventFusionRule.Publish publish = rule.publish();
            log("规则的所有步骤均已执行完成，进入事件发布阶段");

            // 评估发布条件
            try {
                if (!evaluateCondition(publish.condition()))
                    logAndAbort("规则的发布条件判断为false，规则执行终止");
            } catch (ExpressionEvaluationException ex) {
                logAndAbort("规则在判断发布条件时发生异常：" + ex.getMessage() + "，规则执行终止");
            }

            // 计算事件融合输出
            Map<String, Object> outputPayload = new HashMap<>();
            try {
                outputPayload = evaluateParams(publish.output());
            } catch (ExpressionEvaluationException ex) {
                logAndAbort("规则在准备发布事件的数据时发生异常：" + ex.getMessage() + "，规则执行终止");
            }

            // 构建发布事件
            long timestamp = System.currentTimeMillis();
            String datePart = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                                           .format(DateTimeFormatter.ofPattern("MMdd-HHmm"));
            String randomPart = String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
            String identifier = "fused-" + publish.spaceEventId() + "-" + datePart + "-" + randomPart;
            DataEvent result = DataEvent.builder()
                                        .timestamp(timestamp)
                                        .sourceIngestor("EventFusionEngine")
                                        .eventSource(spaceEvent)
                                        .identifier(identifier)
                                        .eventId(publish.spaceEventId())
                                        .payload(outputPayload)
                                        .build();
            this.publishEvent = result;

            // 返回结果
            log("规则发布事件构造完成，事件ID：" + identifier);
            persistLogs(true);
            return Optional.of(result);
        }

        /** 中止规则执行 */
        private Optional<DataEvent> abort() {
            this.persistLogs(false);
            return Optional.empty();
        }


        // ==========================================
        // 辅助函数(构建依赖图、查找可执行步骤、日志记录等)
        // ==========================================

        /** 查找所有可执行的步骤 */
        private List<EventFusionRule.Step> findExecutableSteps() {
            return rule
                .steps().stream()
                .filter(step -> stepStatusMap.get(step.stepId()) == StepStatus.PENDING)
                .filter(step -> dependencies
                    .get(step.stepId())
                    .stream()
                    .allMatch(prev -> stepStatusMap.get(prev) == StepStatus.COMPLETED)
                )
                .toList();
        }

        /** 构建步骤依赖关系图 */
        private Map<String, List<String>> buildDependencies(List<EventFusionRule.Step> steps) {
            Map<String, List<String>> dependencies = steps
                .stream()
                // 构建所有依赖关系，假设 A.next = [B, C] 且 B.next = C 则构建 (B -> A), (C -> A), (C -> B)
                .flatMap(step -> step.next().stream().map(next -> Map.entry(next, step.stepId())))
                // 按照 Key 分组，比如 B -> [A], C -> [A, B]
                .collect(Collectors.groupingBy(
                    Map.Entry::getKey,
                    HashMap::new,
                    Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
            // 确保每个步骤至少有一个空依赖列表，假设 D 是起始节点，则设置 D -> []
            steps.forEach(step -> dependencies.putIfAbsent(step.stepId(), new ArrayList<>()));
            return dependencies;
        }

        private void log(String message) {
            logs.add(message);
        }

        private void logAndAbort(String message) throws FlowAbortException {
            log(message);
            throw new FlowAbortException();
        }

        /** 将运行上下文持久化到数据库中，包括触发数据、步骤中间数据、运行日志、结果事件数据等。 */
        private void persistLogs(boolean isSuccess) {
            var runHistory = new EventFusionRunHistory();
            runHistory.setRuleName(rule.ruleName());
            runHistory.setTriggers(triggers);
            runHistory.setStepOutputs(stepOutputs);
            runHistory.setPublishedEvent(publishEvent);
            runHistory.setLogs(logs);
            runHistory.setIsSuccess(isSuccess);
            runHistoryRepository.save(runHistory);
        }

        /** 格式化 stepId，过长时进行截断显示 */
        private String formatStepId(String stepId) {
            if (stepId == null) return "";
            if (stepId.length() <= 6) return stepId;
            return stepId.substring(0, 3) + "..." + stepId.substring(stepId.length() - 3);
        }


        // ==========================================
        // 算子调用逻辑
        // ==========================================

        private Map<String, Object> invokeOperator(EventFusionRule.Step step, Map<String, Object> inputArgs)
            throws OperatorException {

            var opType = step.operatorType();
            if (opType == null) {
              throw new OperatorException("算子类型 operatorType 为 null.");
            } else if (opType == common && !StringUtils.hasText(step.operatorName())) {
                throw new OperatorException("当前算子类型为common时, operatorName 为 null.");
            } else if (opType == service && !StringUtils.hasText(step.operatorUrl())) {
                throw new OperatorException("当前算子类型为service时, operatorUrl 为 null.");
            } else if (opType == service && step.operatorHttpMethod() == null) {
                throw new OperatorException("当前算子类型为service时, operatorHttpMethod 为 null.");
            }

            try {
                switch (opType) {
                    case common -> {
                        return commonOperatorRegistry.getOperator(step.operatorName()).calculate(inputArgs);
                    }
                    case service -> {
                        return RestClient
                            .builder().requestFactory(new SimpleClientHttpRequestFactory()).build()
                            .method(HttpMethod.valueOf(step.operatorHttpMethod().name()))
                            .uri(step.operatorUrl())
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(inputArgs)
                            .retrieve()
                            .body(new ParameterizedTypeReference<>() {});
                    }
                    default -> throw new OperatorException("不支持的算子类型: " + step.operatorType());
                }
            } catch (Exception e) {
                log.error("[事件融合引擎] 算子调用过程中发生异常: ", e);
                throw new OperatorException("(%s): %s".formatted(e.getClass().getSimpleName(), e.getMessage()));
            }
        }

        private static class OperatorException extends Exception {
            private OperatorException(String message) {super(message);}
        }


        // ==========================================
        // 表达式评估逻辑
        // ==========================================

        /** 对参数列表进行求值： 将 List&lt;Param&gt; 转化为 Map&lt;String, Object&gt; */
        private Map<String, Object> evaluateParams(List<Param> params) throws ExpressionEvaluationException {
            Map<String, Object> result = new HashMap<>();
            for (Param param : params) {
                result.put(param.key(), evaluateExpression(param.expr()));
            }
            return result;
        }

        /**
         * 评估返回值为 Object 的表达式，调用了 evalExpression(..., Object.class)
         * @see #evalExpression(String, Class)
         */
        private Object evaluateExpression(@Nullable String spelExpr) throws ExpressionEvaluationException {
            if (spelExpr == null) return null;
            return evalExpression(spelExpr, Object.class);
        }

        /**
         * 评估返回值为 Boolean 的条件表达式，调用了 evalExpression(..., Boolean.class)
         * @see #evaluateExpression(String)
         */
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private boolean evaluateCondition(@Nullable String spelExpr) throws ExpressionEvaluationException {
            if (spelExpr == null || spelExpr.isBlank()) return true;
            return evalExpression(spelExpr, Boolean.class);
        }

        /** 使用 SpEL 对表达式进行求值，返回值的类型由泛型参数 T 决定 */
        private <T> T evalExpression(
            @NotNull String expression, @NotNull Class<T> type
        ) throws ExpressionEvaluationException {

            // 构建 SpEL 上下文
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.addPropertyAccessor(new MapAccessor());
            context.setVariable("triggers", this.triggersInput);
            context.setVariable("stepOutputs", this.stepOutputs);

            try {
                return new SpelExpressionParser().parseExpression(expression).getValue(context, type);
            } catch (RuntimeException e) {
                throw new ExpressionEvaluationException(
                    "对表达式: " + expression + "\n" +
                    "求值时发生异常, 原因: (" + e.getClass().getSimpleName() + ") " + e.getMessage() + "\n" +
                    "With triggers: " + triggersInput + "\n" +
                    "     stepOutputs: " + stepOutputs + "\n"
                );
            }
        }

        private static class ExpressionEvaluationException extends Exception {
            private ExpressionEvaluationException(String message) {super(message);}
        }
    }
}


