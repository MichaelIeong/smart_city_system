package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.AppGridRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.AppRuleRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AppGridService {
    @Resource
    private AppRuleRepository appRuleRepository;

    public List<AppRuleInfo> getAppList(String gridId) {
        return appRuleRepository.findByGridId(gridId);
    }

    public List<AppRuleInfo> findByGridId(String meshId) {
        return List.of();
    }
}
