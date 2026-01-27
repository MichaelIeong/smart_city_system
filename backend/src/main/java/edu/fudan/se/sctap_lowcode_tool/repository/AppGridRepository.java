package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.AppGrid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppGridRepository extends JpaRepository<AppGrid, Integer> {
    List<AppGrid> findByAppRuleId(Integer appId);

    AppGrid findByAppRuleIdAndGridId(Integer appId, String gridId);

    @Modifying
    @Query("DELETE FROM AppGrid g WHERE g.appRuleId IN ?1")
    void deleteByAppRuleIdIn(List<Integer> appRuleIds);
}
