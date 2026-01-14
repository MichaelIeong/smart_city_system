package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.vo.EnvISResourceVO;
import edu.fudan.se.sctap_lowcode_tool.service.EnvISResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/is_resources")
public class EnvISResourceController {

    @Autowired
    private EnvISResourceService envISResourceService;

    @GetMapping("/information")
    public List<EnvISResourceVO> getEnvInformationResources() {
        return envISResourceService.findAllInformation(); // 实际调用处
    }

    @GetMapping("/social")
    public List<EnvISResourceVO> getEnvSocialResources() {
        return envISResourceService.findAllSocial();
    }
}