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
        System.out.println("八嘎八嘎");
        RuleInfo existRule = fusionRepository.findByRuleName(ruleInfo.getRuleName());
        if(existRule!=null){
            System.out.println("nima,尼玛嗦斯");
            System.out.println(existRule.getRuleName());
            System.out.println(existRule.getRuleId());
            ruleInfo.setRuleId(existRule.getRuleId());
            fusionRepository.save(ruleInfo);
        }else{
            fusionRepository.save(ruleInfo);
        }
        return true;
    }

    @Override
    public List<RuleInfo> getRuleList(){
        return fusionRepository.findAll();
    }


}
