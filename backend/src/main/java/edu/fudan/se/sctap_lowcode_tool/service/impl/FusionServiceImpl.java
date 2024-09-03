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
        fusionRepository.save(ruleInfo);
        return true;
    }

    @Override
    public List<RuleInfo> getRuleList(){
        return fusionRepository.findAll();
    }

}
