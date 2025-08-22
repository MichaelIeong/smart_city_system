package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.FusionRuleBranch;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FusionRuleBranchRepository extends JpaRepository<FusionRuleBranch, Long> {

    List<FusionRuleBranch> findByRule_RuleId(Integer ruleId);

    // ✅ 按 ruleId + spaceId 计算最大的 branch_index（spaceId 可为 null）
    @Query("""
           select coalesce(max(b.branchIndex), 0)
           from FusionRuleBranch b
           where b.rule.ruleId = :ruleId
             and ((:spaceId is null and b.space is null) or (b.space.spaceId = :spaceId))
           """)
    int findMaxIndex(@Param("ruleId") Integer ruleId, @Param("spaceId") Integer spaceId);

    // ✅ 选择一个用于执行的分支：优先 active，其次 index 最小；space 可选
    @Query("""
           select b from FusionRuleBranch b
           where b.rule.ruleId = :ruleId
             and (:spaceId is null or b.space.spaceId = :spaceId)
           order by case when b.status = 'active' then 0 else 1 end, b.branchIndex asc
           """)
    List<FusionRuleBranch> pickOneForExecution(@Param("ruleId") Integer ruleId,
                                               @Param("spaceId") Integer spaceId);
}