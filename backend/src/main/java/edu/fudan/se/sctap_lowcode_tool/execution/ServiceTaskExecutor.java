package edu.fudan.se.sctap_lowcode_tool.execution;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.service.FunctionExeService;
import edu.fudan.se.sctap_lowcode_tool.service.SocialServiceExeService;
//import edu.fudan.se.sctap_lowcode_tool.service.InformationServiceExeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ServiceTaskExecutor {

    @Autowired
    private FunctionExeService functionExeService;

    @Autowired
    private SocialServiceExeService socialServiceExeService;
//
//    @Autowired
//    private InformationServiceExeService informationServiceExeService;

    public void execute(JsonNode node) {
        String nodeId = node.get("id").asText();
        String nodeType = node.get("type").asText();  // 获取节点类型

        // 处理起始节点（start），直接触发下游任务
        if ("start".equals(nodeType)) {
            System.out.println("跳过起始节点: " + nodeId);
            return;
        }

        System.out.println("执行节点: " + nodeId + "，类型: " + nodeType);

        switch (nodeType) {
            case "Device Service":
                executeDeviceService(node);
                break;
            case "Information Service":
                executeInformationService(node);
                break;
            case "Social Service":
                executeSocialService(node);
                break;
            default:
                System.out.println("跳过未知类型的节点: " + nodeId);
        }
    }

    private void executeDeviceService(JsonNode node) {
        String serviceName = node.has("deviceService") ? node.get("deviceService").asText() : "";

        if (serviceName == null || serviceName.isEmpty()) {
            System.out.println("跳过无设备服务的节点: " + node.get("id").asText());
            return;
        }

        System.out.println("执行设备服务: " + serviceName);
        Integer serviceId = functionExeService.findIdByName(serviceName);
        String apiUrl = functionExeService.getApiUrl(serviceId, serviceName);
        callApi(apiUrl);
    }

    private void executeInformationService(JsonNode node) {
        String resourceName = node.has("informationResource") ? node.get("informationResource").asText() : "";

        if (resourceName == null || resourceName.isEmpty()) {
            System.out.println("跳过无信息资源的节点: " + node.get("id").asText());
            return;
        }

        System.out.println("执行信息服务: " + resourceName);
        //String apiUrl = informationServiceExeService.getApiUrl(resourceName);
        //callApi(apiUrl);
    }

    private void executeSocialService(JsonNode node) {
        String socialResource = node.has("socialResource") ? node.get("socialResource").asText() : "";

        if (socialResource == null || socialResource.isEmpty()) {
            System.out.println("跳过无社交资源的节点: " + node.get("id").asText());
            return;
        }

        System.out.println("执行社交服务: " + socialResource);
        String apiUrl = socialServiceExeService.getApiUrl(socialResource);
        callApi(apiUrl);
    }

    private void callApi(String apiUrl) {
        System.out.println("调用 API: " + apiUrl);
        try {
            TimeUnit.SECONDS.sleep(1);  // 模拟 API 调用时间
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}