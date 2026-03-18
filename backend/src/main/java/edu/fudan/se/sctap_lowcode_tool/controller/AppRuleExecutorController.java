package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.AlertMessage;
import edu.fudan.se.sctap_lowcode_tool.DTO.AppRuleCompleteRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.EventTriggerRequest;
import edu.fudan.se.sctap_lowcode_tool.service.AppRuleExecutorService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tapExecutor")
public class AppRuleExecutorController {
    @Resource
    private AppRuleExecutorService appRuleExecutorService;

    /**
     * 事件触发应用规则
     * */
    @PostMapping("/trigger")
    public void triggerAppRule(@RequestBody EventTriggerRequest eventTriggerRequest) {
        appRuleExecutorService.triggerAppRule(eventTriggerRequest);
    }

    /**
     * 动作完成上报
     * */
    @PostMapping("/complete")
    public void complete(@RequestBody AppRuleCompleteRequest appRuleCompleteRequest) {
        appRuleExecutorService.complete(appRuleCompleteRequest);
    }

    /**
     * 获取日志
     * */
    @GetMapping("/getLog")
    public ResponseEntity<List<String>> getLog(
            @RequestParam("appId") Integer appId,
            @RequestParam("waitValue") String waitValue) {
        List<String> logs = appRuleExecutorService.getLog(appId, waitValue);
        return ResponseEntity.ok(logs);
    }

    /**
     * 接收边缘端消息
     * */
    @PostMapping("/receiveMessage")
    public void receiveMessage(@RequestBody AlertMessage alertMessage) {
        appRuleExecutorService.receiveEdgeMessage(alertMessage);
    }
}
