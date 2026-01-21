package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.SocialResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.SocialResourceRepository;
import edu.fudan.se.sctap_lowcode_tool.service.SocialResourceService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/socialResources")
public class SocialResourceController {

    @Autowired
    private SocialResourceService socialResourceService;

    @Resource
    private SocialResourceRepository socialResourceRepository;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<SocialResourceInfo>> getSocialResourcesByProjectId(@PathVariable Integer projectId) {
        System.out.println(socialResourceService.getSocialResourceByProjectId(projectId));
        return ResponseEntity.ok(socialResourceService.getSocialResourceByProjectId(projectId));
    }

    //获取所有的社会服务名称
    @GetMapping("/services")
    public List<Map<String, String>> getTypes() {
        return socialResourceService.getSocialResourceTypes();
    }

    //根据服务名称获取对应的details
    @GetMapping("/details")
    public String getDetails(@RequestParam String description) {
        return socialResourceService.getMoreDetails(description);
    }

    //根据服务名称获取参数列表
    @GetMapping("/params")
    public String getParams(@RequestParam String description){
        return socialResourceService.getParamJson(description);
    }

    @PostMapping("/add")
    public ResponseEntity<SocialResourceInfo> addSocialResource(@RequestBody SocialResourceInfo info) {
        return ResponseEntity.ok(socialResourceRepository.save(info));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteSocialResource(@PathVariable Integer id) {
        socialResourceRepository.deleteById(id);
        return ResponseEntity.ok("Deleted successfully");
    }


}
