package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.AppGrid;
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

    @Resource
    private AppGridRepository appGridRepository;

    public List<AppRuleInfo> getAppList(String gridId) {
        return appRuleRepository.findByGridId(gridId);
    }

    public List<AppGrid> findByAppRuleId(Integer appRuleId) {
        return appGridRepository.findByAppRuleId(appRuleId);
    }
}
