package edu.fudan.se.sctap_lowcode_tool.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.fudan.se.sctap_lowcode_tool.DTO.*;
import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.model.ServiceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.AppRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.ProjectRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.PropertySpaceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.ServiceRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.ConditionEvaluator;
import edu.fudan.se.sctap_lowcode_tool.utils.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AppRuleService {

    @Autowired
    private AppRuleRepository appRuleRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PropertySpaceRepository propertySpaceRepository;

    @Autowired
    private ServiceRepository serviceRepository;

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
    }

    public void createRule(AppRuleCreateRequest rule) {
        var appRuleInfo = getEntityFromRequest(rule);
        appRuleRepository.save(appRuleInfo);
    }

    public void updateRule(Integer ruleId, AppRuleCreateRequest rule) {
        var appRuleInfo = getEntityFromRequest(rule);
        if (appRuleRepository.findById(ruleId).isEmpty()) {
            throw new BadRequestException(
                    "400", "Rule not exists to update",
                    "rule.id", ruleId.toString(), "ruleId not found"
            );
        }
        appRuleInfo.setId(ruleId);
        appRuleRepository.save(appRuleInfo);
    }

    private AppRuleInfo getEntityFromRequest(AppRuleCreateRequest rule) {
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

    private AppRuleContent getContentFromJson(String json) {
        return new GsonBuilder().create().fromJson(json, AppRuleContent.class);
    }

    private List<AppRuleContent> getAllRules() {
        return appRuleRepository.findAll().stream()
                .map(rule -> this.getContentFromJson(rule.getRuleJson()))
                .toList();
    }

    private boolean filterTrigger(AppRuleContent rule, AppRuleRunRequest request) {
        /*
        - 遍历该规则的每个Trigger元组 AS (event_id, space_is, timestamp_op, timestamp_value)
        ---- 若event_id不匹配, 则跳过
        ---- 若space_is不为空, 判断是否匹配
        ---- 若timestamp_op不为空, 判断与当前时间是否匹配
        - 只要有任意一个元组匹配就返回True
         */
        for (int i = 0; i < rule.scenarioTrigger().size(); i++) {
            // 匹配event_id
            if (!rule.scenarioTrigger().getEventType(i).map(request.eventId()::equals).orElse(false)) {
                continue;
            }
            // 匹配space_is
            if (!rule.scenarioTrigger().satisfySpaceCondition(i, request.spaceId())) {
                continue;
            }
            // 匹配timestamp
            if (!rule.scenarioTrigger().satisfyTimeCondition(i)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean filterCurrentCondition(AppRuleContent rule, AppRuleRunRequest request) {
        /*
        - 若Trigger被触发, 检查current_condition是否满足
        -- 若为空, 视为满足
        -- 若不为空, 则解析current_condition, 检查是否满足。解析时将trigger_location绑定到实际的space
         */
        ConditionEvaluator parser = new ConditionEvaluator() {
            @Override
            public double get(String variable) {
                String[] parts = variable.split("\\.");
                if (parts.length != 2) {
                    throw new BadRequestException(
                            "400", "Variable format error",
                            "variable", variable, "variable format error"
                    );
                }
                var spaceId = parts[0].replace("trigger_location", request.spaceId());
                var propertyId = parts[1];
                var projectId = request.projectId();
                var value = propertySpaceRepository.findPropertyValue(spaceId, propertyId, projectId);
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException | NullPointerException e) {
                    return Double.NaN;
                }
            }
        };
        return parser.evaluateCondition(rule.getAction().currentCondition());
    }

    private void postService(AppRuleContent rule, AppRuleRunRequest request) {
        /*
        遍历action中action_location的所有位置作为space_id:
        -- 在数据库中寻找service_id, project_id 和 space_id对应的service,
        -- POST该请求的url
        请求体BODY为rule.Scenario_Action.action
        */
        var action = rule.getAction().action();
        var actionJson = new Gson().toJson(action);
        var serviceName = action.actionName();
        action.actionLocations().stream()
                .map(space -> space.replace("trigger_location", request.spaceId()))
                .map(space -> serviceRepository.findSpecificService(serviceName, space, request.projectId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ServiceInfo::getUrl)
                .forEach(url -> HttpRequestUtils.post(url, actionJson));
    }

    public void runRule(AppRuleRunRequest request) {
        var rules = this.getAllRules();
        for (var rule : rules) {
            if (this.filterTrigger(rule, request) && this.filterCurrentCondition(rule, request)) {
                log.info("Rule {} is triggered", rule.getAction().action().actionName());
                this.postService(rule, request);
            }
        }
    }
    
    


}
