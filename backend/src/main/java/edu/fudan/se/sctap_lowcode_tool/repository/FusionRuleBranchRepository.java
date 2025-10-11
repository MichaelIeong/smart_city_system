package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.FusionRuleBranch;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FusionRuleBranchRepository extends JpaRepository<FusionRuleBranch, Integer> {

    // 根据主干规则 ID 查询所有分支
    List<FusionRuleBranch> findByRule_RuleId(Integer ruleId);

    // 选择一个用于执行的分支：优先 active，其次按 branchId（替代原 branchIndex）
    @Query("""
            select b from FusionRuleBranch b
            where b.rule.ruleId = :ruleId
              and (:spaceId is null or b.space.spaceId = :spaceId)
            order by case when b.status = 'active' then 0 else 1 end,
                     b.branchId asc
            """)
    List<FusionRuleBranch> pickOneForExecution(@Param("ruleId") Integer ruleId,
                                               @Param("spaceId") Integer spaceId);

    // 判断该主干在某空间（含 null）是否已有分支 —— “套用到可达空间”时用于过滤
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

    // （可选）取出现有分支列表
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

    // 删除指定规则下的所有分支（若使用此方法，记得在调用处包事务）
    @Modifying
    @Query("delete from FusionRuleBranch b where b.rule.ruleId = :ruleId")
    void deleteByRule_RuleId(@Param("ruleId") Integer ruleId);
}