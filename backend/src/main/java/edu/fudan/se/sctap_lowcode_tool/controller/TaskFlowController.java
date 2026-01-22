package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.service.TaskFlowService;
import io.jsonwebtoken.lang.Arrays;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * 新版任务执行引擎入口
 * 采用 Post 方式，支持服务名调用及灵活参数透传
 */
@RestController
@RequestMapping("/api/task-flow") // 使用新的路径前缀
public class TaskFlowController {

    @Autowired
    private TaskFlowService taskFlowService;

    @Operation(summary = "服务执行引擎入口(新)", description = "通过服务名调用执行引擎，并支持传入自定义JSON参数")
    @PostMapping("/executeByName")
    public List<String> executeByName(@RequestParam String serviceName, @RequestBody Map<String, Object> serviceParams) {
        try {
            // 1. 调用你原来的异步方法
            CompletableFuture<List<String>> future = taskFlowService.executeByName(serviceName, serviceParams);

            // 2. 关键点：使用 .join()。这会阻塞住，直到所有的异步节点跑完，拿到最终日志。
            List<String> resultLogs = future.join();

            // 3. 直接返回 List<String>，完全符合他的接口定义
            return resultLogs;

        } catch (Exception e) {
            // 报错也返回一个带 [ERROR] 格式的列表，保证他不崩溃
            return List.of(String.format("[ERROR]-[%s]: 系统内部错误 - %s", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")), 
                e.getMessage()));
        }
    }
}