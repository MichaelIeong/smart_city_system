//package edu.fudan.se.sctap_lowcode_tool.controller;
//
//import edu.fudan.se.sctap_lowcode_tool.DTO.AppRuleCompleteRequest;
//import edu.fudan.se.sctap_lowcode_tool.DTO.EventTriggerRequest;
//import edu.fudan.se.sctap_lowcode_tool.service.AppRuleExecutorService;
//import jakarta.annotation.Resource;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/tapExecutor")
//public class AppRuleExecutorController {
//    @Resource
//    private AppRuleExecutorService appRuleExecutorService;
//
//    /**
//     * 事件触发应用规则
//     * */
//    @PostMapping("/trigger")
//    public void triggerAppRule(@RequestBody EventTriggerRequest eventTriggerRequest) {
//        appRuleExecutorService.triggerAppRule(eventTriggerRequest);
//    }
//
//    /**
//     * 动作完成上报
//     * */
//    @PostMapping("/complete")
//    public void complete(@RequestBody AppRuleCompleteRequest appRuleCompleteRequest) {
//        appRuleExecutorService.complete(appRuleCompleteRequest);
//    }
//
//    /**
//     * 获取tsl事件数据
//     * */
//    @GetMapping("/tsl/getEventData")
//    public ResponseEntity<List<Map<String, Object>>> getTslEventData(@RequestParam("pageNum") int pageNum, @RequestParam("pageSize") int pageSize) {
//        List<Map<String, Object>> eventData = appRuleExecutorService.getTslEventData(pageNum, pageSize);
//        return ResponseEntity.ok(eventData);
//    }
//
//    /**
//     * 获取正在运行的事件
//     * */
//    @GetMapping("/getRunningEvents")
//    public ResponseEntity<List<Map<String, Object>>> getRunningEvents() {
//        List<Map<String, Object>> eventData = appRuleExecutorService.getRunningEvents();
//        return ResponseEntity.ok(eventData);
//    }
//
//    /**
//     * 获取某一事件的所有执行标识
//     * */
//    @GetMapping("/getWaitValueOfEvent")
//    public ResponseEntity<List<String>> getWaitValueOfEvent(@RequestParam("eventType") String eventType) {
//        List<String> waitValues = appRuleExecutorService.getWaitValueOfEvent(eventType);
//        return ResponseEntity.ok(waitValues);
//    }
//
//    /**
//     * 获取日志
//     * */
//    @GetMapping("/getLog")
//    public ResponseEntity<List<String>> getLog(
//            @RequestParam("eventType") String eventType,
//            @RequestParam("waitValue") String waitValue) {
//        List<String> logs = appRuleExecutorService.getLog(eventType, waitValue);
//        return ResponseEntity.ok(logs);
//    }
//}
