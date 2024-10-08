package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.service.FusionRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FusionRuleServiceImpl implements FusionRuleService {

    @Autowired
    private FusionRuleRepository fusionRuleRepository;

    @Override
    public void addNewRule(FusionRule fusionRule) {
        System.out.println("八嘎八嘎");
        FusionRule existRule = fusionRuleRepository.findByRuleName(fusionRule.getRuleName());
        if(existRule!=null){
            System.out.println("nima,尼玛嗦斯");
            System.out.println(existRule.getRuleName());
            System.out.println(existRule.getRuleId());
            fusionRule.setRuleId(existRule.getRuleId());
            fusionRuleRepository.save(fusionRule);
        }else{
            fusionRuleRepository.save(fusionRule);
        }
    }

    @Override
    public List<FusionRule> getRuleList() {
        return fusionRuleRepository.findAll();
    }


}
