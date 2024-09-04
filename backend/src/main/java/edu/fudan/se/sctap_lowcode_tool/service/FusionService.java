package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.RuleInfo;

import java.util.List;

public interface FusionService {

    boolean addNewRule(RuleInfo ruleInfo);

    List<RuleInfo>  getRuleList();
}
