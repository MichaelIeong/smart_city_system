package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;
import edu.fudan.se.sctap_lowcode_tool.model.Operator;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.OperatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NodeRedService {

    @Autowired
    private FusionRuleRepository fusionRuleRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private OperatorService operatorService;

    public void addNewRule(FusionRule fusionRule) {
        FusionRule existRule = fusionRuleRepository.findByRuleName(fusionRule.getRuleName());
        if (existRule != null) {
            fusionRule.setRuleId(existRule.getRuleId());
        }
        fusionRuleRepository.save(fusionRule);
    }

    public FusionRule getRuleById(int ruleId) {
        return fusionRuleRepository.findById(ruleId).orElse(null);
    }

    public List<Operator> getAllOperators() {
        List<Operator> operators = new ArrayList<>();
        operators.addAll(operatorService.getAllUtilOperators());
        operators.addAll(operatorRepository.findAll());
        return operators;
    }
}