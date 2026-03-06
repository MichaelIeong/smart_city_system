package edu.fudan.se.sctap_lowcode_tool.execution;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CompositeServiceDispatcher {

    @Autowired
    private AtomicServiceExecutor atomicExecutor;

    /**
     * 执行图逻辑 - 异步版
     * @return 返回一个包含所有执行轨迹日志的 Future
     */
    public CompletableFuture<List<String>> dispatch(ServiceGraph graph, Map<String, Object> runtimeParams) {
        // 0. 初始化线程安全的日志篮子
        List<String> executionLogs = Collections.synchronizedList(new ArrayList<>());

        // 1. 参数校验逻辑
        try {
            validateInputs(graph, runtimeParams);
        } catch (Exception e) {
            executionLogs.add("参数校验失败: " + e.getMessage());
            return CompletableFuture.completedFuture(executionLogs);
        }

        Map<String, ServiceTaskNode> allNodes = graph.getNodes();
        Map<String, AtomicInteger> remainingDependencies = new ConcurrentHashMap<>();

        // 初始化依赖计数器
        allNodes.forEach((id, node) -> {
            int depCount = node.getDependencies().size();
            remainingDependencies.put(id, new AtomicInteger(depCount));
        });

        // 2. 找出所有“初始节点”
        List<ServiceTaskNode> readyNodes = allNodes.values().stream()
                .filter(node -> node.getDependencies().isEmpty())
                .toList();

        if (readyNodes.isEmpty() && !allNodes.isEmpty()) {
            executionLogs.add("错误：检测到环路，无法开始执行");
            return CompletableFuture.completedFuture(executionLogs);
        }

        // 3. 递归触发执行
        // 我们需要把所有分支的 Future 收集起来
        List<CompletableFuture<Void>> startFutures = readyNodes.stream()
                .map(node -> processNodeAsync(node, graph, runtimeParams, remainingDependencies, executionLogs))
                .toList();

        // 4. [关键] 使用 allOf 等待所有节点链条执行完毕
        return CompletableFuture.allOf(startFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    return executionLogs;
                });
    }

    private CompletableFuture<Void> processNodeAsync(ServiceTaskNode node, ServiceGraph graph, 
                                                Map<String, Object> runtimeParams, 
                                                Map<String, AtomicInteger> depCounter,
                                                List<String> logs) {
        JsonNode rawStep = node.getRawNode();
        Map<String, Object> finalArgs = prepareArgs(rawStep, runtimeParams);
        String location = (String) runtimeParams.getOrDefault("location", "unknown_location");
        // --- 步骤 B: 异步执行 (Execution) ---
        return CompletableFuture.supplyAsync(() -> {
            String type = rawStep.path("type").asText();
                String resultLog = "";
                switch (type.toLowerCase()) {
                    case "cyber": 
                        resultLog = atomicExecutor.executeCyber(rawStep,location, finalArgs); 
                        break;
                    case "physical": 
                        resultLog = atomicExecutor.executePhysical(rawStep, location, finalArgs); 
                        break;
                    case "social": 
                        resultLog = atomicExecutor.executeSocial(rawStep, finalArgs); 
                        break;
                }
                logs.add(resultLog); 
                return null;
        }).thenCompose(v -> {
            // --- 步骤 C: 完成回调与推进 (Propagation) ---
            List<CompletableFuture<Void>> nextFutures = new ArrayList<>();
            
            for (String nextId : node.getNextIds()) {
                ServiceTaskNode nextNode = graph.getNodes().get(nextId);
                int remaining = depCounter.get(nextId).decrementAndGet();
                
                if (remaining == 0) {
                    // 递归触发下游
                    nextFutures.add(processNodeAsync(nextNode, graph, runtimeParams, depCounter, logs));
                }
            }
            
            // 等待所有子分支完成
            return CompletableFuture.allOf(nextFutures.toArray(new CompletableFuture[0]));
        });
        }

    private void validateInputs(ServiceGraph graph, Map<String, Object> runtimeParams) {
        JsonNode inputsDef = graph.getInputsDefinition();
        if (inputsDef != null && inputsDef.isArray()) {
            for (JsonNode input : inputsDef) {
                String name = input.get("name").asText();
                String expectedType = input.get("type").asText();
                Object value = runtimeParams.get(name);
                if (value == null) throw new RuntimeException("缺失参数: " + name);
                validateAndConvert(name, expectedType, value, runtimeParams);
            }
        }
    }

    private Map<String, Object> prepareArgs(JsonNode rawStep, Map<String, Object> runtimeParams) {
        JsonNode argMapping = rawStep.get("args");
        Map<String, Object> finalArgs = new HashMap<>();
        if (argMapping != null) {
            argMapping.fields().forEachRemaining(entry -> {
                finalArgs.put(entry.getKey(), runtimeParams.get(entry.getValue().asText()));
            });
        }
        return finalArgs;
    }

    private void validateAndConvert(String name, String type, Object value, Map<String, Object> params) {
        try {
            if ("int".equalsIgnoreCase(type)) params.put(name, Integer.parseInt(value.toString()));
            else if ("string".equalsIgnoreCase(type)) params.put(name, value.toString());
        } catch (Exception e) {
            throw new RuntimeException("参数 [" + name + "] 类型错误");
        }
    }
    // 日志格式化方法
    private String formatLog(String level, String message) {
        String timestamp = java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm:ss"));
        // 按照你同学的代码逻辑拼接：[level]-[timestamp]: message
        return String.format("[%s]-[%s]: %s", level, timestamp, message);
    }
}