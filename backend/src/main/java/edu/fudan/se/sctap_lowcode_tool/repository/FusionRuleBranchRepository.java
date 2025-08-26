package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.FusionRuleBranch;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FusionRuleBranchRepository extends JpaRepository<FusionRuleBranch, Long> {

    List<FusionRuleBranch> findByRule_RuleId(Integer ruleId);

    // 计算某主干 + 空间（可为 null）下的最大分支序号
    @Query("""
            select coalesce(max(b.branchIndex), 0)
            from FusionRuleBranch b
            where b.rule.ruleId = :ruleId
              and (
                  (:spaceId is null and b.space is null)
                  or (:spaceId is not null and b.space.spaceId = :spaceId)
              )
            """)
    int findMaxIndex(@Param("ruleId") Integer ruleId, @Param("spaceId") Integer spaceId);

    // 选择一个用于执行的分支：优先 active，其次 index 最小；space 可选
    @Query("""
            select b from FusionRuleBranch b
            where b.rule.ruleId = :ruleId
              and (:spaceId is null or b.space.spaceId = :spaceId)
            order by case when b.status = 'active' then 0 else 1 end, b.branchIndex asc
            """)
    List<FusionRuleBranch> pickOneForExecution(@Param("ruleId") Integer ruleId,
                                               @Param("spaceId") Integer spaceId);

    // ★ 判断该主干在某空间（含 null）是否已有分支 —— 用于“套用到可达空间”时跳过已存在的
    @Query("""
            select (count(b) > 0) from FusionRuleBranch b
            where b.rule.ruleId = :ruleId
              and (
                  (:spaceId is null and b.space is null)
                  or (:spaceId is not null and b.space.spaceId = :spaceId)
              )
            """)
    boolean existsByRuleAndSpace(@Param("ruleId") Integer ruleId,
                                 @Param("spaceId") Integer spaceId);

    // （可选）如果你需要取出现有分支列表而不只是判断存在性：
    @Query("""
            select b from FusionRuleBranch b
            where b.rule.ruleId = :ruleId
              and (
                  (:spaceId is null and b.space is null)
                  or (:spaceId is not null and b.space.spaceId = :spaceId)
              )
            """)
    List<FusionRuleBranch> findByRuleAndSpace(@Param("ruleId") Integer ruleId,
                                              @Param("spaceId") Integer spaceId);
}