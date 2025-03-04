package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.execution.ServiceTaskExecutor;
import edu.fudan.se.sctap_lowcode_tool.execution.TaskScheduler;
import edu.fudan.se.sctap_lowcode_tool.execution.WorkflowParser;
import edu.fudan.se.sctap_lowcode_tool.model.ServiceInfo;
import edu.fudan.se.sctap_lowcode_tool.service.ServiceService;
import edu.fudan.se.sctap_lowcode_tool.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private SpaceService spaceService;

    private final TaskScheduler scheduler; // 让 Spring 负责管理



    private final WorkflowParser parser;

    @Autowired
    public ServiceController(WorkflowParser parser, TaskScheduler scheduler) {
        this.parser = parser;
        this.scheduler = scheduler;
    }

    @GetMapping
    public ResponseEntity<Iterable<ServiceBriefResponse>> getServicesByProjectId(
            @RequestParam(name = "project") String projectId) {
        System.out.println(serviceService.findAllByProjectId(projectId));
        return ResponseEntity.ok(serviceService.findAllByProjectId(projectId));
    }

    /**
     * 获取规则列表。
     *
     * @param request Http 请求对象
     * @return 规则列表
     */
    @Operation(summary = "获取规则列表", description = "将规则列表传给前端")
    @GetMapping("/getServiceList")
    public ResponseEntity<?> getServiceList(HttpServletRequest request) {
        List<ServiceInfo> serviceRuleList = serviceService.getServiceList();
        return ResponseEntity.ok(serviceRuleList);
    }

    @Operation(summary = "上传新的服务", description = "用户在node-red组合好服务，传给后端，加入到数据库")
    @PostMapping("/uploadservice")
    public ResponseEntity<Void> saveService(@RequestBody JsonNode serviceMsg) {
        String compositionName = "";
        String projectId = "";
        Integer serviceId = 2;
        // 遍历 JSON 数组
        System.out.println(serviceMsg);
        for (JsonNode node : serviceMsg) {
            // 获取 type = "tab" 的 serviceId
            if (node.has("type") && "tab".equals(node.get("type").asText())) {
                JsonNode envNode = node.get("env");
                if (envNode != null && envNode.isArray()) {
                    for (JsonNode env : envNode) {
                        if ("serviceId".equals(env.get("name").asText())) {
                            serviceId = env.get("value").asInt();
                        }
                    }
                }
            }

            // 获取 Composition 里面的 compositionName 和 projectId
            if (node.has("type") && "Composition".equals(node.get("type").asText())) {
                compositionName = Optional.ofNullable(node.get("compositionName"))
                        .map(JsonNode::asText)
                        .orElse(null);
                projectId = Optional.ofNullable(node.get("projectId"))
                        .map(JsonNode::asText)
                        .orElse(null);
            }
        }

        System.out.println("serviceId: " + serviceId);
        System.out.println("compositionName: " + compositionName);
        System.out.println("projectId: " + projectId);
        System.out.println(626565);
        System.out.println(compositionName);
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServiceJson(serviceMsg.toString());
        serviceInfo.setServiceName(compositionName);
        serviceInfo.setProjectId(projectId);
        serviceInfo.setServiceId(serviceId);
        serviceInfo.setParentingSpace(spaceService.findSpaceById(1).get());
        serviceService.addOrUpdateService(serviceInfo);

        // System.out.println(serviceMsg.get("compositionName"));

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "查看CSP", description = "返回服务的CSP模型")
    @GetMapping ("/getCSP")
    public ResponseEntity<?> getCSP(@RequestParam Integer serviceId) {
        ServiceInfo serviceInfo = serviceService.getService(serviceId);
        //System.out.println(serviceInfo.getServiceCsp());
        return ResponseEntity.ok(serviceInfo.getServiceCsp());
    }

    @Operation(summary = "手动生成CSP", description = "手敲的CSP，放到数据库")
    @PostMapping("/generateCSPbyHand")
    public ResponseEntity<Void> CSPbyHand(@RequestParam Integer serviceId, @RequestBody String Csp){
        ServiceInfo serviceInfo = serviceService.getService(serviceId);
        serviceInfo.setServiceCsp(Csp);
        serviceService.addOrUpdateService(serviceInfo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "LLM生成CSP", description = "用LLM生成CSP，放到数据库")
    @GetMapping("/generateCSPbyLLM")
    public ResponseEntity<?> CSPbyLLM(@RequestParam Integer serviceId){
        try {
            // 调用服务层生成CSP
            String csp = serviceService.generateCSP(serviceId);
            // 返回生成的CSP
            return ResponseEntity.ok(csp);
        } catch (Exception e) {
            // 错误处理
            return ResponseEntity.status(500).body("Error generating CSP: " + e.getMessage());
        }
    }

    @Operation(summary = "服务执行引擎入口", description = "调用执行引擎，执行响应服务")
    @GetMapping("/executorService")
    public ResponseEntity<?> executorService(@RequestParam Integer serviceId) throws Exception {
        // 1. 获取服务的json
        ServiceInfo serviceInfo = serviceService.getService(serviceId);
        String serviceJson = serviceInfo.getServiceJson();
        // 调用执行引擎
        // 2. 初始化工作流解析器
        //WorkflowParser parser = new WorkflowParser(serviceJson);
        parser.initParser(serviceJson);
        System.out.println(parser.getNodeMap());
        System.out.println(parser.getDependencies());
        System.out.println(parser.getStartNodeId());

        // 3. 初始化执行引擎
//        ServiceTaskExecutor serviceTaskExecutor = new ServiceTaskExecutor();
//        TaskScheduler scheduler = new TaskScheduler(parser, serviceTaskExecutor);

        // 4. 执行工作流
        scheduler.start(parser.getStartNodeId());

        // 5. 关闭线程池
        //Thread.sleep(30000);
        // scheduler.shutdown();
        return ResponseEntity.ok().build();
    }
}


//通过serviceId获取node-red的json，通过json，获取里面的deviceId，通过deviceId获取LHA