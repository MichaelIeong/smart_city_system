package edu.fudan.se.sctap_lowcode_tool.execution;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class TaskScheduler {
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final Map<String, JsonNode> nodeMap;
    private final Map<String, List<String>> dependencies;
    private final Map<String, Integer> pendingDependencies;
    private final ServiceTaskExecutor serviceTaskExecutor;

    @Autowired
    public TaskScheduler(ServiceTaskExecutor serviceTaskExecutor, WorkflowParser parser) {
        this.serviceTaskExecutor = serviceTaskExecutor;
        this.nodeMap = parser.getNodeMap();  // ✅ 从 WorkflowParser 获取数据
        this.dependencies = parser.getDependencies();
        this.pendingDependencies = parser.getPendingDependencies();
    }


    public void start(String startNodeId) {
        executeNode(startNodeId);
    }

    private void executeNode(String nodeId) {
        JsonNode node = nodeMap.get(nodeId);
        if (node == null) return;

        executorService.submit(() -> {
            try {
                serviceTaskExecutor.execute(node);

                // 处理依赖的下游节点
                List<String> nextNodes = dependencies.get(nodeId);
                if (nextNodes != null) {
                    for (String nextNode : nextNodes) {
                        synchronized (pendingDependencies) {
                            pendingDependencies.put(nextNode, pendingDependencies.get(nextNode) - 1);
                            if (pendingDependencies.get(nextNode) == 0) {
                                executeNode(nextNode);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void shutdown() {
        executorService.shutdown(); // 停止接收新任务
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // 超时后强制关闭
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}