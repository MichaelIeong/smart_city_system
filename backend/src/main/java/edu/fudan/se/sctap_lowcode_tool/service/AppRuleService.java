package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.AppRuleRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.AppRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AppRuleService {

    @Autowired
    private AppRuleRepository appRuleRepository;

    @Autowired
    private ProjectRepository projectRepository;

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

    public void createRule(AppRuleRequest rule) {
        var appRuleInfo = getEntityFromRequest(rule);
        appRuleRepository.save(appRuleInfo);
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


}
