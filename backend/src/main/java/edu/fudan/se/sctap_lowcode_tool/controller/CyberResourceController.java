package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.CyberResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.service.CyberResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cyberResources")
public class CyberResourceController {

    @Autowired
    private CyberResourceService cyberResourceService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<CyberResourceInfo>> getCyberResourcesByProjectId(@PathVariable Integer projectId) {
        System.out.println(cyberResourceService.getCyberResourceByProjectId(projectId));
        return ResponseEntity.ok(cyberResourceService.getCyberResourceByProjectId(projectId));
    }

    @GetMapping("/services")
    public List<Map<String, String>> getTypes() {
        return cyberResourceService.getCyberResourceTypes();
    }

    //根据服务名称获取对应的details
    @GetMapping("/details")
    public String getDetails(@RequestParam String description) {
        return cyberResourceService.getMoreDetails(description);
    }

    //根据服务名称获取参数列表
    @GetMapping("/params")
    public String getParams(@RequestParam String description){
        return cyberResourceService.getParamJson(description);
    }

}
