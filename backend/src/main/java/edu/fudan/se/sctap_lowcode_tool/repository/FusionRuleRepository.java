package edu.fudan.se.sctap_lowcode_tool.repository;


import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FusionRuleRepository extends JpaRepository<FusionRule, Integer> {

    FusionRule findByRuleName(String ruleName);
}
