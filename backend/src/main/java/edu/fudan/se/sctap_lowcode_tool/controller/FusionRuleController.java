package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.fusion.BranchDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.fusion.RuleWithCountDTO;
import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;
import edu.fudan.se.sctap_lowcode_tool.service.FusionRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/fusion")
@Tag(name = "FusionController", description = "事件融合控制器")
public class FusionRuleController {

    @Autowired
    private FusionRuleService fusionRuleService;

    // ========= 规则（主干）接口 =========

    @Operation(summary = "获取规则列表", description = "将规则列表传给前端（主干列表）")
    @GetMapping("/getRuleList")
    public ResponseEntity<List<FusionRule>> getRuleList() {
        List<FusionRule> fusionRuleList = fusionRuleService.getRuleList();
        return ResponseEntity.ok(fusionRuleList);
    }

    @Operation(summary = "删除规则", description = "根据 ruleId 删除规则及其关联的分支")
    @DeleteMapping("/deleteRule/{ruleId}")
    public ResponseEntity<String> deleteRuleById(@PathVariable int ruleId) {
        boolean deleted = fusionRuleService.deleteRuleById(ruleId);
        return deleted ? ResponseEntity.ok("删除成功")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("规则未找到");
    }

    @Operation(summary = "获取规则可执行的空间列表（含名称）", description = "返回 [{id,name}]")
    @GetMapping("/executableSpaces/{ruleId}")
    public ResponseEntity<List<Map<String, Object>>> getExecutableSpaces(@PathVariable int ruleId) {
        try {
            List<Map<String, Object>> spaces = fusionRuleService.getExecutableSpaces(ruleId);
            return ResponseEntity.ok(spaces);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "修改主干名称")
    @PutMapping("/rules/{ruleId}")
    public ResponseEntity<String> renameRule(@PathVariable int ruleId, @RequestBody Map<String, Object> body) {
        Object v = body.get("ruleName");
        String newName = (v == null) ? null : String.valueOf(v).trim();
        if (newName == null || newName.isEmpty()) {
            return ResponseEntity.badRequest().body("ruleName 不能为空");
        }
        boolean ok = fusionRuleService.updateRuleName(ruleId, newName);
        return ok ? ResponseEntity.ok("更新成功")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("规则未找到");
    }

    // （可选）规则列表附带分支数，便于前端展示
    @Operation(summary = "规则列表（含分支数量）", description = "主干规则及其分支数量统计")
    @GetMapping("/rulesWithCounts")
    public ResponseEntity<List<RuleWithCountDTO>> rulesWithCounts() {
        List<FusionRule> rules = fusionRuleService.getRuleList();
        List<RuleWithCountDTO> out = rules.stream().map(r -> {
            RuleWithCountDTO dto = new RuleWithCountDTO();
            dto.setRuleId(r.getRuleId());
            dto.setRuleName(r.getRuleName());
            dto.setBranchCount(fusionRuleService.countBranchesOfRule(r.getRuleId()));
            return dto;
        }).toList();
        return ResponseEntity.ok(out);
    }

    // ========= 分支接口（重要：用 DTO 返回，避免懒加载序列化问题） =========

    @Operation(summary = "列出某条规则的分支", description = "返回主干(ruleId)下的所有分支（以 DTO 形式）")
    @GetMapping("/rules/{ruleId}/branches")
    public ResponseEntity<List<BranchDTO>> listBranches(@PathVariable int ruleId) {
        List<BranchDTO> branches = fusionRuleService.listBranchesByRule(ruleId)
                .stream().map(BranchDTO::from).toList();
        return ResponseEntity.ok(branches);
    }

    @Operation(summary = "执行分支", description = "根据 branchId 执行具体分支")
    @PostMapping("/executeBranch/{branchId}")
    public ResponseEntity<String> executeBranch(@PathVariable int branchId) {
        boolean ok = fusionRuleService.executeBranch(branchId);
        return ok ? ResponseEntity.ok("执行分支成功")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("分支未找到");
    }

    @Operation(summary = "暂停分支", description = "暂停该分支（等价于暂停其所属主干的当前执行）")
    @PutMapping("/pauseBranch/{branchId}")
    public ResponseEntity<String> pauseBranch(@PathVariable int branchId) {
        boolean ok = fusionRuleService.pauseBranch(branchId);
        return ok ? ResponseEntity.ok("暂停分支成功")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("分支未找到或无法暂停");
    }

    @Operation(summary = "删除分支", description = "根据 branchId 删除分支")
    @DeleteMapping("/branches/{branchId}")
    public ResponseEntity<String> deleteBranch(@PathVariable int branchId) {
        boolean ok = fusionRuleService.deleteBranch(branchId);
        return ok ? ResponseEntity.ok("删除分支成功")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("分支未找到");
    }

    @Operation(
            summary = "把可达规则套用到其它可执行空间",
            description = "依据能力匹配结果，为每个可执行空间创建一个分支；已存在分支的空间将跳过"
    )
    @PostMapping("/rules/{ruleId}/applyToExecutableSpaces")
    public ResponseEntity<Map<String, Object>> applyToExecutableSpaces(
            @PathVariable int ruleId,
            @RequestParam(name = "activate", defaultValue = "false") boolean activateNewBranches
    ) {
        Map<String, Object> result = fusionRuleService.applyRuleToExecutableSpaces(ruleId, activateNewBranches);
        return ResponseEntity.ok(result);
    }
}