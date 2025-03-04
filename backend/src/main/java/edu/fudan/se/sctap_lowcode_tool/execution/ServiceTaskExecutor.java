package edu.fudan.se.sctap_lowcode_tool.execution;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.service.FunctionExeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ServiceTaskExecutor {

    @Autowired
    private FunctionExeService functionExeService;

    public void execute(JsonNode node) {
        String nodeId = node.get("id").asText();
        String nodeType = node.get("type").asText();  // 获取节点类型

        // 处理起始节点（start），直接触发下游任务
        if ("start".equals(nodeType)) {
            System.out.println("跳过起始节点: " + nodeId);
            return;
        }

        String serviceName = node.has("deviceService") ? node.get("deviceService").asText() : "";

        if (serviceName.isEmpty()) {
            System.out.println("跳过无设备服务的节点: " + nodeId);
            return;
        }

        System.out.println("执行节点: " + nodeId + "，设备服务: " + serviceName);

        // 先在 serviceService（表）用 serviceName 获取到 functionId
        Integer serviceId = functionExeService.findIdByName(serviceName);
        String apiUrl = functionExeService.getApiUrl(serviceId, serviceName);

        callApi(apiUrl);
    }

    private void callApi(String apiUrl) {
        System.out.println("调用 API: " + apiUrl);
        try {
            TimeUnit.SECONDS.sleep(1); // 模拟 API 调用时间
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}