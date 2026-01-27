package edu.fudan.se.sctap_lowcode_tool.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceJson;
import edu.fudan.se.sctap_lowcode_tool.execution.ServiceTaskExecutor;
import edu.fudan.se.sctap_lowcode_tool.execution.TaskScheduler;
import edu.fudan.se.sctap_lowcode_tool.execution.WorkflowParser;
import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import edu.fudan.se.sctap_lowcode_tool.model.ServiceInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.ServiceNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.SpaceNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.SpaceNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.service.ServiceService;
import edu.fudan.se.sctap_lowcode_tool.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private SpaceService spaceService;

    private final TaskScheduler scheduler; // 让 Spring 负责管理

    @Autowired
    private ObjectMapper objectMapper;
    private final WorkflowParser parser;
    @Autowired
    private SpaceNodeRepository spaceNodeRepository;

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
        // 这个是从mysql
        List<ServiceInfo> serviceRuleList = serviceService.getServiceList();
        // 这个是从neo4j
        // List<ServiceNode> serviceRuleList = serviceService.getServiceNodeList();

        return ResponseEntity.ok(serviceRuleList);
    }

    @Operation(summary = "根据项目ID获取规则列表", description = "按 projectId 查询服务列表")
    @GetMapping("/getServiceListByProject")
    public ResponseEntity<?> getServiceListByProject(@RequestParam("projectId") String projectId) {
        System.out.println(projectId);
        List<ServiceInfo> serviceRuleList = serviceService.getServiceListByProjectId(projectId);
        return ResponseEntity.ok(serviceRuleList);
    }

    @Operation(summary = "上传新的服务", description = "用户在node-red组合好服务，传给后端，加入到数据库")
    @PostMapping("/uploadservice")
    public ResponseEntity<Void> saveService(@RequestBody JsonNode serviceMsg) {
        String serviceName = "";
        String description = "";
        String projectId = "";

        // 遍历 JSON 数组
        for (JsonNode node : serviceMsg) {
            // 获取 Composition 节点的 serviceName / description / projectId
            if (node.has("type") && "Composition".equals(node.get("type").asText())) {
                serviceName = Optional.ofNullable(node.get("compositionNameLabel")) // 下拉框显示的服务名
                        .map(JsonNode::asText)
                        .orElse(null);
                description = Optional.ofNullable(node.get("description"))
                        .map(JsonNode::asText)
                        .orElse("");
                projectId = Optional.ofNullable(node.get("projectId"))
                        .map(JsonNode::asText)
                        .orElse(null);
            }
        }

        // ===== MySQL 保存 =====
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServiceJson(serviceMsg.toString());
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setDescription(description);
        serviceInfo.setProjectId(projectId);

        serviceService.addOrUpdateService(serviceInfo);

        // System.out.println(serviceMsg.get("compositionName"));
        // 这里是neo4j
//        ServiceNode serviceNode = new ServiceNode();
//        serviceNode.setServiceJson(serviceMsg.toString());
//        serviceNode.setServiceId(serviceId);
//        serviceNode.setDescription("这是一个服务");
//        serviceNode.setServiceName(compositionName);
//        SpaceNode currentSpace = spaceNodeRepository.findBySpaceId(spaceId).get();

//        serviceNode.setParentingSpace(currentSpace);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "服务执行引擎入口", description = "调用执行引擎，执行响应服务")
    @GetMapping("/executorService")
    public ResponseEntity<?> executorService(@RequestParam Integer serviceId) throws Exception {
        serviceService.executeServiceById(serviceId);
        return ResponseEntity.ok().build();
    }

    /*
    @PostMapping("/uploadCompositionService")
    public ResponseEntity<Void> saveCompositionService(@RequestBody String standardData,
                                                       @RequestParam("gridId") String gridId){
        JSONObject jsonObj = JSONObject.parseObject(standardData);
        EnvService envService = new EnvService();
        envService.setServiceJson(standardData);
        envService.setServiceName(jsonObj.getString("action_name"));
        envService.setDescription(jsonObj.getString("description"));
        envService.setName(jsonObj.getString(""));
        if(gridId.equals("crossRegion")) {
            envService.setCrossRegion(true);
        } else {
            envService.setCrossRegion(false);
        }
        serviceService.saveCompositionService(envService);
        return ResponseEntity.ok().build();
    }
     */

    @PostMapping("/uploadCompositionService")
    public ResponseEntity<Void> saveService(@RequestBody ServiceJson serviceJson,
                                            @RequestParam("gridId") String gridId){
        try {
            String compositionJson = objectMapper.writeValueAsString(serviceJson.getCompositionJson());
            String totalJson = objectMapper.writeValueAsString(serviceJson.getTotalJson());
            String deviceTypeArray = serviceJson.getDeviceTypeArray().toString();

            JSONObject jsonObj_comp = JSONObject.parseObject(compositionJson);
            System.out.println(jsonObj_comp.getString("action_name"));
            EnvService envService = new EnvService();
            envService.setServiceJson(compositionJson);
            envService.setRuleJson(totalJson);
            envService.setServiceName(jsonObj_comp.getString("action_name"));
            envService.setDescription(jsonObj_comp.getString("description"));
            if(gridId.equals("crossRegion")) {
                envService.setCrossRegion(true);
            } else {
                envService.setCrossRegion(false);
            }
            envService.setDependDtypes(deviceTypeArray);
            serviceService.saveCompositionService(envService);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/uploadDeviceServiceType")
    public ResponseEntity<Void> saveDeviceTypeService(@RequestBody String deviceTypeArray){
        EnvService envService = new EnvService();
        envService.setDependDtypes(deviceTypeArray);
        serviceService.saveCompositionService(envService);
        return ResponseEntity.ok().build();
    }

}