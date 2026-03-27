package edu.fudan.se.sctap_lowcode_tool.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceJson;
import edu.fudan.se.sctap_lowcode_tool.constant.RoleConstant;
import edu.fudan.se.sctap_lowcode_tool.execution.TaskScheduler;
import edu.fudan.se.sctap_lowcode_tool.execution.WorkflowParser;
import edu.fudan.se.sctap_lowcode_tool.model.EdgeNode;
import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import edu.fudan.se.sctap_lowcode_tool.model.EnvServiceGrid;
import edu.fudan.se.sctap_lowcode_tool.model.ServiceInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.SpaceNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EdgeNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvServiceGridRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvServiceRepository;
import edu.fudan.se.sctap_lowcode_tool.service.ServiceService;
import edu.fudan.se.sctap_lowcode_tool.service.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
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

    @Value("${app.node-role:edge}")
    private String nodeRole;

    @Autowired
    private EdgeNodeRepository edgeNodeRepository;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private EnvServiceRepository envServiceRepository;

    @Resource
    private EnvServiceGridRepository envServiceGridRepository;

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
                                            @RequestParam("gridId") String gridId,
                                            @RequestParam("projectId") Integer projectId,
                                            @RequestParam(value = "id", required = false) Integer id){
        try {
            String compositionJson = objectMapper.writeValueAsString(serviceJson.getCompositionJson());
            String totalJson = objectMapper.writeValueAsString(serviceJson.getTotalJson());
            List<String> deviceTypeArray = (List<String>) serviceJson.getDeviceTypeArray();
            String nameCN = serviceJson.getNameCN();
            JSONObject jsonObj_comp = JSONObject.parseObject(compositionJson);
            System.out.println(jsonObj_comp.getString("action_name"));
            EnvService envService = new EnvService();
            envService.setServiceJson(compositionJson);
            envService.setRuleJson(totalJson);
            envService.setServiceName(jsonObj_comp.getString("action_name"));
            envService.setDescription(jsonObj_comp.getString("description"));
            envService.setProjectId(projectId);
            envService.setName(nameCN);
            if(gridId.equals("crossRegion")) {
                envService.setCrossRegion(true);
            } else {
                envService.setCrossRegion(false);
            }
            envService.setDependDtypes(deviceTypeArray);
            envService.setCreateTime(LocalDateTime.now());
            // 如果是边缘节点，使用云端节点的id
            if(RoleConstant.EDGE.equals(nodeRole)) {
                envService.setId(id);
            }
            envServiceRepository.save(envService);
            if(!envService.getCrossRegion()) {
                EnvServiceGrid envServiceGrid = new EnvServiceGrid();
                envServiceGrid.setGridId(gridId);
                envServiceGrid.setEnvServiceId(envService.getId());
                envServiceGrid.setEnabled(true);
                envServiceGridRepository.save(envServiceGrid);
            }
            // 如果当前是云端节点，保存完毕后，向边端下发请求，并带上刚刚生成的 ID
            if(RoleConstant.CLOUD.equals(nodeRole)) {
                // 将 envService.getId() 传递给下发方法
                dispatchCompositionService(serviceJson, gridId, projectId, envService.getId());
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 根据 gridId 将请求下发给对应的边缘节点
     */
    private void dispatchCompositionService(ServiceJson serviceJson, String gridId, Integer projectId, Integer cloudGeneratedId) {
        if (!"crossRegion".equals(gridId)) {
            EdgeNode targetNode = edgeNodeRepository.findByGridId(gridId);
            if (targetNode != null) {
                sendToEdge(targetNode, serviceJson, gridId, projectId, cloudGeneratedId);
            }
        }
    }

    /**
     * 执行 HTTP POST 请求，发送给边端（带上 id 参数）
     */
    private void sendToEdge(EdgeNode node, ServiceJson serviceJson, String gridId, Integer projectId, Integer id) {
        String ipAddress = node.getIpAddress();
        // 注意这里：URL 后面追加了 &id={id}
        String url = ipAddress + "/api/services/uploadCompositionService?gridId={gridId}&projectId={projectId}&id={id}";
        try {
            // 使用 RestTemplate 的占位符特性，安全地传入 gridId, projectId, 和 id
            restTemplate.postForEntity(
                    url,
                    serviceJson,
                    Void.class,
                    gridId,
                    projectId,
                    id // 对应 URL 中的 {id}
            );
            log.info("边缘节点 [{}] 服务组合下发成功", ipAddress);
        } catch (Exception e) {
            log.error("向边缘节点 [{}] 服务组合下发失败，网络或服务异常: {}", ipAddress, e.getMessage());
        }
    }

//    @PostMapping("/uploadDeviceServiceType")
//    public ResponseEntity<Void> saveDeviceTypeService(@RequestBody List<String> deviceTypeArray){
//        EnvService envService = new EnvService();
//        envService.setDependDtypes(deviceTypeArray);
//        serviceService.saveCompositionService(envService);
//        return ResponseEntity.ok().build();
//    }

}