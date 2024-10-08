package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;

import java.util.List;

public interface FusionRuleService {

    void addNewRule(FusionRule fusionRule);

    List<FusionRule> getRuleList();

}
