package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.model.ServiceInfo;
import edu.fudan.se.sctap_lowcode_tool.service.ServiceService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @GetMapping
    public ResponseEntity<Iterable<ServiceBriefResponse>> getServicesByProjectId(
            @RequestParam(name = "project") String projectId) {
        return ResponseEntity.ok(serviceService.findAllByProjectId(projectId));
    }

    @Operation(summary = "上传新的服务", description = "用户在node-red组合好服务，传给后端，加入到数据库")
    @PostMapping("/uploadservice")
    public ResponseEntity<Void> saveService(@RequestBody JsonNode serviceMsg) {
        String compositionName = "";
        String projectId = "";
        // 遍历 JSON 数组
        for (JsonNode node : serviceMsg) {
            if (node.has("type") && "Composition".equals(node.get("type").asText())) {
                compositionName = node.has("compositionName") ? node.get("compositionName").asText() : "No Name";
                projectId = node.has("projectId") ? node.get("projectId").asText() : "No projectId";
                break;
            }
        }
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServiceJson(serviceMsg.toString());
        serviceInfo.setServiceName(compositionName);
        serviceService.addOrUpdateService(serviceInfo);

        // System.out.println(serviceMsg.get("compositionName"));

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "查看CSP", description = "返回服务的CSP模型")
    @GetMapping ("/getCSP")
    public ResponseEntity<?> getCSP(@RequestParam String serviceId) {
        ServiceInfo serviceInfo = serviceService.getService(serviceId);
        //System.out.println(serviceInfo.getServiceCsp());
        return ResponseEntity.ok(serviceInfo.getServiceCsp());
    }

    @Operation(summary = "手动生成CSP", description = "手敲的CSP，放到数据库")
    @PostMapping("/generateCSPbyHand")
    public ResponseEntity<Void> CSPbyHand(@RequestParam String serviceId, @RequestBody String Csp){
        ServiceInfo serviceInfo = serviceService.getService(serviceId);
        serviceInfo.setServiceCsp(Csp);
        serviceService.addOrUpdateService(serviceInfo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "LLM生成CSP", description = "用LLM生成CSP，放到数据库")
    @GetMapping("/generateCSPbyLLM")
    public ResponseEntity<?> CSPbyLLM(@RequestParam String serviceId){
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
}


//通过serviceId获取node-red的json，通过json，获取里面的deviceId，通过deviceId获取LHA