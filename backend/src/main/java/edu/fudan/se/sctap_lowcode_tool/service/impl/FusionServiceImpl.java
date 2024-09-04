package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.RuleInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRepository;
import edu.fudan.se.sctap_lowcode_tool.service.FusionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FusionServiceImpl implements FusionService {

    @Autowired
    private FusionRepository fusionRepository;

    @Override
    public boolean addNewRule(RuleInfo ruleInfo){
        Optional<RuleInfo> existingRule = fusionRepository.findByRulename(ruleInfo.getRulename());
        if (existingRule.isPresent()) {
            // 如果记录存在，更新现有记录
            RuleInfo ruleToUpdate = existingRule.get();
            // 更新字段
            ruleToUpdate.setOtherFields(ruleInfo.getOtherFields()); // 复制其他字段
            fusionRepository.save(ruleToUpdate);
        } else {
            // 如果记录不存在，保存为新记录
            fusionRepository.save(ruleInfo);
        }
        return true;
    }

    @Override
    public List<RuleInfo> getRuleList(){
        return fusionRepository.findAll();
    }

}
