package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.event_fusion_2026_jan.SpaceEventHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpaceEventHistoryRepository extends JpaRepository<SpaceEventHistory, Integer>, JpaSpecificationExecutor<SpaceEventHistory> {
}
