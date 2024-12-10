package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FusionRuleService {

    // 注入 FusionRuleRepository，用于操作 FusionRule 的数据
    @Autowired
    private FusionRuleRepository fusionRuleRepository;

    /**
     * 添加或更新规则。
     * 如果规则名称已经存在，则更新规则的内容；
     * 如果规则不存在，则保存为新规则。
     *
     * @param fusionRule 要添加或更新的规则对象
     */
    public void addNewRule(FusionRule fusionRule) {
        // 根据规则名称查询数据库中是否已有该规则
        FusionRule existRule = fusionRuleRepository.findByRuleName(fusionRule.getRuleName());

        // 如果规则已存在，更新其 ID，以便在保存时覆盖旧记录
        if (existRule != null) {
            System.out.println(existRule.getRuleName()); // 打印已存在规则的名称
            System.out.println(existRule.getRuleId());   // 打印已存在规则的 ID
            fusionRule.setRuleId(existRule.getRuleId()); // 设置已有规则的 ID
        }

        // 保存规则：
        // 如果规则的 ID 存在，则更新该规则；
        // 否则，插入为新规则。
        fusionRuleRepository.save(fusionRule);
    }

    /**
     * 获取所有规则的列表。
     *
     * @return 数据库中所有规则的列表
     */
    public List<FusionRule> getRuleList() {
        // 调用 repository 的方法查询所有规则
        return fusionRuleRepository.findAll();
    }
}