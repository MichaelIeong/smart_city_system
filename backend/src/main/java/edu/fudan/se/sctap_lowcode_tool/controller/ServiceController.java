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
            @RequestParam(name = "project") int projectId) {
        return ResponseEntity.ok(serviceService.findAllByProjectId(projectId));
    }

    @Operation(summary = "上传新的服务", description = "用户在node-red组合好服务，传给后端，加入到数据库")
    @PostMapping("/uploadservice")
    public ResponseEntity<Void> saveService(@RequestBody JsonNode serviceMsg) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServiceJson(serviceMsg.toString());
        serviceInfo.setServiceName(serviceMsg.get("serviceName").asText());
        serviceService.addOrUpdateService(serviceInfo);
        System.out.println(serviceMsg);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "查看CSP", description = "返回服务的CSP模型")
    @GetMapping ("/getCSP")
    public ResponseEntity<?> getCSP(@PathVariable String serviceId) {
        ServiceInfo serviceInfo = serviceService.getService(serviceId);
        //System.out.println(serviceMsg);
        return ResponseEntity.ok(serviceInfo.getServiceCsp());
    }

    @Operation(summary = "手动生成CSP", description = "手敲的CSP，放到数据库")
    @PostMapping("/generateCSPbyHand")
    public ResponseEntity<Void> CSPbyHand(@PathVariable String serviceId, @RequestBody String Csp){
        ServiceInfo serviceInfo = serviceService.getService(serviceId);
        serviceInfo.setServiceCsp(Csp);
        serviceService.addOrUpdateService(serviceInfo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "LLM生成CSP", description = "用LLM生成CSP，放到数据库")
    @GetMapping("/generateCSPbyLLM")
    public ResponseEntity<?> CSPbyLLM(@PathVariable String serviceId){
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
