package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan.EventFusionRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventFusionRuleRepository extends JpaRepository<EventFusionRuleEntity, Integer> {
}
