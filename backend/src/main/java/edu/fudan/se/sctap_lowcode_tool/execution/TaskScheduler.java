package edu.fudan.se.sctap_lowcode_tool.execution;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class TaskScheduler {

    private final WorkflowParser parser;
    private final ServiceTaskExecutor serviceTaskExecutor;
    private final ExecutorService executorService;

    // 运行时上下文（保存 Composition 中的 spaceId 等信息）
    private final Map<String, Object> context = new HashMap<>();

    @Autowired
    public TaskScheduler(WorkflowParser parser, ServiceTaskExecutor executor) {
        this.parser = parser;
        this.serviceTaskExecutor = executor;
        this.executorService = Executors.newFixedThreadPool(4); // 固定线程池
    }

    /**
     * 启动调度器
     */
    public void start(String startNodeId) {
        System.out.println("TaskScheduler 启动，开始节点 = " + startNodeId);

        Queue<String> queue = new LinkedList<>();
        queue.add(startNodeId);

        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            JsonNode node = parser.getNodeMap().get(nodeId);

            if (node == null) continue;

            System.out.println("执行节点: " + node);

            try {
                handleNode(node);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 加入后继节点
            List<String> successors = parser.getDependencies().getOrDefault(nodeId, new ArrayList<>());
            queue.addAll(successors);
        }
    }

    /**
     * 根据节点类型执行不同逻辑
     */
    private void handleNode(JsonNode node) throws Exception {
        String type = node.get("type").asText();

        switch (type) {
            case "Composition":
                handleCompositionNode(node);
                break;

            case "Device Service":
                handleDeviceServiceNode(node);
                break;

            // TODO: 未来扩展其他节点类型，比如 Social Service / Information Service
            default:
                System.out.println("未知类型节点: " + type);
                break;
        }
    }

    /**
     * Composition 节点：保存 spaceId 到上下文
     */
    private void handleCompositionNode(JsonNode node) {
        if (node.has("space")) {
            Integer spaceId = node.get("space").asInt();
            context.put("spaceId", spaceId);
            System.out.println("Composition 节点设置 spaceId = " + spaceId);
        }
        if (node.has("compositionName")) {
            context.put("compositionName", node.get("compositionName").asText());
        }
    }

    /**
     * Device Service 节点：根据 spaceId、deviceType、deviceService 找到具体设备方法并调用
     */
    private void handleDeviceServiceNode(JsonNode node) {
        Integer spaceId = (Integer) context.get("spaceId");
        if (spaceId == null) {
            throw new RuntimeException("执行 Device Service 节点时，未找到 Composition 的 spaceId");
        }

        // 异步提交执行
        Future<?> future = executorService.submit(() -> {
            serviceTaskExecutor.executeDeviceTypeTask(node, spaceId);
        });

        try {
            future.get(); // 等待执行完成，也可以不阻塞
        } catch (Exception e) {
            throw new RuntimeException("执行 Device Service 节点失败", e);
        }
    }

    /**
     * 停止调度器，关闭线程池
     */
    public void shutdown() {
        executorService.shutdown();
        System.out.println("TaskScheduler 已关闭线程池");
    }
}