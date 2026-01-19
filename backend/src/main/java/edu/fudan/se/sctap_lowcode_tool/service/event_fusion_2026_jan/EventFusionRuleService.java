package edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan;

import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.Param;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.Var;
import edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan.EventFusionRuleEntity;
import edu.fudan.se.sctap_lowcode_tool.repository.EventFusionRuleRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h3>EventFusionRuleService 事件融合规则服务</h3>
 * 负责 EventFusionRule 的<b>增删改查</b>操作，具体包括：
 * <ul>
 * <li>规则的创建或更新（包括创建时校验合法性）</li>
 * <li>规则的读取</li>
 * <li>规则的删除</li>
 * </ul>
 * @author Lin Yicheng
 * @since 2026-01-11
 */
@RequiredArgsConstructor
@Service
public class EventFusionRuleService {

    private final EventFusionRuleRepository eventFusionRuleRepository;

    /**
     * 校验事件融合规则的有效性
     *
     * @param rule 待校验的事件融合规则
     * @throws BadRequestException 如果规则配置不符合要求
     */
    private void checkRuleValidity(@NotNull EventFusionRule rule) throws BadRequestException {
        // 1. 当前 triggers 只支持单个事件触发器
        if (rule.triggers().size() != 1) {
            throw new BadRequestException(
                "400", "事件融合规则配置有误",
                "rule.triggers.size", String.valueOf(rule.triggers().size()), "当前仅支持单个事件触发器。"
            );
        }

        // 2. StepId 唯一
        List<String> stepIds = rule.steps().stream().map(EventFusionRule.Step::stepId).toList();
        checkUniqueString(stepIds, "rule.steps[*].stepId");

        // 3. 各 Step 中的 next 引用的 stepId 必须存在，且不能形成环
        // 3.1 检查 next 引用的 stepId 是否存在
        for (EventFusionRule.Step step : rule.steps()) {
            for (String nextStepId : step.next()) {
                if (!stepIds.contains(nextStepId)) {
                    throw new BadRequestException(
                        "400", "事件融合规则配置有误",
                        "rule.steps[" + step.stepId() + "].next", nextStepId, "引用的 stepId 不存在。"
                    );
                }
            }
        }
        // 3.2 检查是否存在环
        if (hasCycle(rule.steps())) {
            throw new BadRequestException(
                "400", "事件融合规则配置有误",
                "rule.steps[*].next", "", "步骤流程中存在环，必须是有向无环图。"
            );
        }

        // 4. 算子类型为 common 时，必须填写 opName。算子类型为 service 时，必须填写 url 和 method。
        rule.steps().forEach(step -> {
            if (step.operatorType() == EventFusionRule.OperatorType.common) {
                if (step.operatorName() == null || step.operatorName().isBlank()) {
                    throw new BadRequestException(
                        "400", "事件融合规则配置有误",
                        "rule.steps[" + step.stepId() + "].operatorName", step.operatorName(), "算子类型为 common 时，必须填写 operatorName。"
                    );
                }
            } else if (step.operatorType() == EventFusionRule.OperatorType.service) {
                if (step.operatorUrl() == null || step.operatorUrl().isBlank()) {
                    throw new BadRequestException(
                        "400", "事件融合规则配置有误",
                        "rule.steps[" + step.stepId() + "].operatorUrl", step.operatorUrl(), "算子类型为 service 时，必须填写 operatorUrl。"
                    );
                }
                if (step.operatorHttpMethod() == null) {
                    throw new BadRequestException(
                        "400", "事件融合规则配置有误",
                        "rule.steps[" + step.stepId() + "].operatorHttpMethod", null, "算子类型为 service 时，必须填写 operatorHttpMethod。"
                    );
                }
            }
        });

        // 5. 各 List<Param> 和 List<Var> 中的 key 必须唯一
        rule.steps().forEach(step -> {
            checkUniqueString(
                step.input().stream().map(Param::key).toList(),
                "rule.steps[" + step.stepId() + "].params[*].key"
            );
            checkUniqueString(
                step.output().stream().map(Var::key).toList(),
                "rule.steps[" + step.stepId() + "].vars[*].key"
            );
        });
        checkUniqueString(
            rule.publish().output().stream().map(Param::key).toList(),
            "rule.publish.output[*].key"
        );

    }

    /**
     * 检查字符串列表中是否存在重复项
     *
     * @param items 待检查的字符串列表
     * @param location 字段位置信息，用于错误提示，可为 null
     * @throws BadRequestException 如果列表中存在重复项
     */
    private void checkUniqueString(@NotNull List<String> items, @Nullable String location) throws BadRequestException {
        if (items.stream().distinct().count() != items.size()) {
            throw new BadRequestException(
                "400", "事件融合规则配置有误",
                location == null ? "unknown" : location,
                items.toString(), "包含重复项。"
            );
        }
    }

    /**
     * 使用 DFS + 三色标记法检测有向图中是否存在环
     * @param steps 步骤列表
     * @return true 如果存在环，否则返回 false
     */
    private boolean hasCycle(@NotNull List<EventFusionRule.Step> steps) {
        // 构建图的邻接表
        Map<String, List<String>> graph = new HashMap<>();
        for (EventFusionRule.Step step : steps) {
            graph.put(step.stepId(), step.next());
        }

        // 三色标记：0-未访问（白色），1-正在访问（灰色），2-已完成（黑色）
        Map<String, Integer> colors = new HashMap<>();
        for (EventFusionRule.Step step : steps) {
            colors.put(step.stepId(), 0);
        }

        // 对每个节点进行 DFS
        for (EventFusionRule.Step step : steps) {
            if (colors.get(step.stepId()) == 0) {
                if (dfsCycleDetect(step.stepId(), graph, colors)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * DFS 环检测辅助方法
     * @param node 当前节点
     * @param graph 邻接表
     * @param colors 节点颜色标记
     * @return true 如果检测到环
     */
    private boolean dfsCycleDetect(String node, Map<String, List<String>> graph, Map<String, Integer> colors) {
        colors.put(node, 1); // 标记为正在访问

        List<String> neighbors = graph.get(node);
        if (neighbors != null) {
            for (String next : neighbors) {
                Integer color = colors.get(next);
                if (color == null) {
                    // 这种情况在之前的检查中应该已经被捕获了（next引用的stepId不存在）
                    continue;
                }
                if (color == 1) {
                    // 发现环：当前DFS路径中再次遇到灰色节点
                    return true;
                }
                if (color == 0) {
                    if (dfsCycleDetect(next, graph, colors)) {
                        return true;
                    }
                }
            }
        }

        colors.put(node, 2); // 标记为已完成
        return false;
    }

    /**
     * 创建或更新事件融合规则
     * <p>
     * 执行流程：
     * <ol>
     *   <li>校验规则的有效性（触发器、步骤流程、算子配置、参数唯一性等）</li>
     *   <li>如果提供了 id，检查对应的规则实体是否存在</li>
     *   <li>保存或更新规则到数据库</li>
     * </ol>
     *
     * @param id 规则 ID，如果为 null 则创建新规则，否则更新现有规则
     * @param rule 事件融合规则定义
     * @return 保存后的事件融合规则实体
     * @throws BadRequestException 如果规则配置无效或更新的 ID 不存在
     */
    public EventFusionRuleEntity createOrUpdateRule(
        @Nullable Integer id,
        @NotNull EventFusionRule rule
    ) throws BadRequestException {
        // 校验规则有效性
        checkRuleValidity(rule);
        // 若 id 不为 null，则检查对应实体是否存在
        if (id != null && !eventFusionRuleRepository.existsById(id)) {
            throw new BadRequestException(
                "404", "试图更新 id 为 " + id  + " 的事件融合规则，该规则不存在。",
                "id", String.valueOf(id), "指定的事件融合规则 ID 不存在，无法更新。"
            );
        }
        // 保存或更新规则
        EventFusionRuleEntity entity = new EventFusionRuleEntity(id, rule);
        return eventFusionRuleRepository.save(entity);
    }

}
