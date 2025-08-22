package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;
import edu.fudan.se.sctap_lowcode_tool.model.FusionRuleBranch;
import edu.fudan.se.sctap_lowcode_tool.service.FusionRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "执行规则（自动挑分支）", description = "优先执行 active 分支，否则执行 index 最小的分支")
    @PostMapping("/executeRule/{ruleId}")
    public ResponseEntity<String> executeRuleById(@PathVariable int ruleId) {
        try {
            boolean executed = fusionRuleService.executeRuleById(ruleId);
            if (executed) {
                return ResponseEntity.ok("执行成功");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("规则未找到或无分支可执行");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("执行失败：" + e.getMessage());
        }
    }

    @Operation(summary = "暂停规则", description = "停止该规则当前后台循环")
    @PutMapping("/pauseRule/{ruleId}")
    public ResponseEntity<String> pauseRuleById(@PathVariable int ruleId) {
        boolean paused = fusionRuleService.pauseRuleById(ruleId);
        return paused ? ResponseEntity.ok("暂停成功")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("规则未找到或无法暂停");
    }

    @Operation(summary = "删除规则", description = "根据 ruleId 删除规则")
    @DeleteMapping("/deleteRule/{ruleId}")
    public ResponseEntity<String> deleteRuleById(@PathVariable int ruleId) {
        boolean deleted = fusionRuleService.deleteRuleById(ruleId);
        return deleted ? ResponseEntity.ok("删除成功")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("规则未找到");
    }

    @Operation(summary = "获取规则可执行的空间列表", description = "检查该规则在哪些空间（location）可以执行")
    @GetMapping("/executableLocations/{ruleId}")
    public ResponseEntity<List<Integer>> getExecutableLocations(@PathVariable int ruleId) {
        try {
            List<Integer> locations = fusionRuleService.getExecutableLocationsForRuleId(ruleId);
            return ResponseEntity.ok(locations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // （可选）规则列表附带分支数，便于前端展示
    @Data
    public static class RuleWithCount {
        private int ruleId;
        private String ruleName;
        private long branchCount;
    }

    @Operation(summary = "规则列表（含分支数量）", description = "主干规则及其分支数量统计")
    @GetMapping("/rulesWithCounts")
    public ResponseEntity<List<RuleWithCount>> rulesWithCounts() {
        List<FusionRule> rules = fusionRuleService.getRuleList();
        List<RuleWithCount> out = rules.stream().map(r -> {
            RuleWithCount dto = new RuleWithCount();
            dto.setRuleId(r.getRuleId());
            dto.setRuleName(r.getRuleName());
            dto.setBranchCount(fusionRuleService.countBranchesOfRule(r.getRuleId()));
            return dto;
        }).toList();
        return ResponseEntity.ok(out);
    }

    // ========= 分支接口（重要：用 DTO 返回，避免懒加载序列化问题） =========

    @Data
    public static class BranchDTO {
        private Long branchId;
        private Integer branchIndex;
        private String branchName;
        private String fusionTarget;
        private String status;
        private String ruleJson;
        private String flowJson;
        private String remark;
        private Integer spaceId; // 仅返回 ID，避免序列化 LAZY 关系

        public static BranchDTO from(FusionRuleBranch b) {
            BranchDTO d = new BranchDTO();
            d.setBranchId(b.getBranchId());
            d.setBranchIndex(b.getBranchIndex());
            d.setBranchName(b.getBranchName());
            d.setFusionTarget(b.getFusionTarget());
            d.setStatus(b.getStatus());
            d.setRuleJson(b.getRuleJson());
            d.setFlowJson(b.getFlowJson());
            d.setRemark(b.getRemark());
            // 实体里提供 getSpaceId()（@Transient）或手动取：b.getSpace() == null ? null : b.getSpace().getSpaceId()
            d.setSpaceId(b.getSpaceId());
            return d;
        }
    }

    @Operation(summary = "列出某条规则的分支", description = "返回主干(ruleId)下的所有分支（以 DTO 形式）")
    @GetMapping("/rules/{ruleId}/branches")
    public ResponseEntity<List<BranchDTO>> listBranches(@PathVariable Integer ruleId) {
        List<BranchDTO> branches = fusionRuleService.listBranchesByRule(ruleId)
                .stream().map(BranchDTO::from).toList();
        return ResponseEntity.ok(branches);
    }

    @Data
    public static class CreateBranchReq {
        private Integer ruleId;
        private Integer spaceId;    // 可为 null
        private String branchName; // 可为 null → 默认“主干名 + 序号”
        private String fusionTarget;
        private String status;     // 可为 null → inactive
        private String ruleJson;
        private String flowJson;
        private String remark;
    }

    @Operation(summary = "创建分支", description = "在主干下创建一个分支；未传 branchName 则自动用“主干名 + 序号”")
    @PostMapping("/branches")
    public ResponseEntity<Long> createBranch(@RequestBody CreateBranchReq req) {
        Long branchId = fusionRuleService.createBranch(
                req.getRuleId(),
                req.getSpaceId(),
                req.getBranchName(),
                req.getFusionTarget(),
                req.getStatus(),
                req.getRuleJson(),
                req.getFlowJson(),
                req.getRemark()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(branchId);
    }

    @Operation(summary = "执行分支", description = "根据 branchId 执行具体分支")
    @PostMapping("/executeBranch/{branchId}")
    public ResponseEntity<String> executeBranch(@PathVariable Long branchId) {
        boolean ok = fusionRuleService.executeBranch(branchId);
        return ok ? ResponseEntity.ok("执行分支成功")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("分支未找到");
    }

    @Operation(summary = "暂停分支", description = "暂停该分支（等价于暂停其所属主干的当前执行）")
    @PutMapping("/pauseBranch/{branchId}")
    public ResponseEntity<String> pauseBranch(@PathVariable Long branchId) {
        boolean ok = fusionRuleService.pauseBranch(branchId);
        return ok ? ResponseEntity.ok("暂停分支成功")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("分支未找到或无法暂停");
    }

    @Operation(summary = "删除分支", description = "根据 branchId 删除分支")
    @DeleteMapping("/branches/{branchId}")
    public ResponseEntity<String> deleteBranch(@PathVariable Long branchId) {
        boolean ok = fusionRuleService.deleteBranch(branchId);
        return ok ? ResponseEntity.ok("删除分支成功")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("分支未找到");
    }
}