package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.model.Operator;
import edu.fudan.se.sctap_lowcode_tool.service.NodeRedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/node-red")
@Tag(name = "NodeRedController", description = "处理 Node-RED 相关操作")
public class NodeRedController {

    @Autowired
    private NodeRedService nodeRedService;

    /**
     * 上传事件融合规则（仅保存）
     */
    @Operation(summary = "上传规则", description = "仅保存规则，不立即执行")
    @PostMapping("/uploadrule")
    public ResponseEntity<Void> uploadRule(@RequestBody Map<String, JsonNode> msg) {
        nodeRedService.handleUploadRule(msg);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取指定项目下所有 Sensor 设备的数据
     */
    @Operation(summary = "获取 Sensor 节点数据", description = "联合查询传给前端")
    @GetMapping("/sensors/{projectId}")
    public ResponseEntity<?> getSensorData(@PathVariable int projectId) {
        return nodeRedService.getSensorDataByProjectId(projectId);
    }

    /**
     * 获取所有事件融合算子
     */
    @Operation(summary = "获取所有事件融合算子", description = "获取所有事件融合算子")
    @GetMapping("/operator/")
    public List<Operator> getAllOperators() {
        return nodeRedService.getAllOperators();
    }

    /**
     * 获取所有事件融合的表名
     */
    @Operation(summary = "获取所有事件融合的表名")
    @GetMapping("/fusion_table/")
    public ResponseEntity<List<String>> getAllFusionTable() {
        List<String> fusionTableList = nodeRedService.getAllFusionTable();
        return ResponseEntity.ok(fusionTableList);
    }
}