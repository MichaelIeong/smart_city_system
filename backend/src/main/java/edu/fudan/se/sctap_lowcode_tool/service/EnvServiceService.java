package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvServiceRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class EnvServiceService {
    @Resource
    private EnvServiceRepository envServiceRepository;

    /**
     * 获取环境级服务列表
     * */
    public List<String> getEnvServiceJsonList(String gridId) {
        List<EnvService> envServices;
        if("crossRegion".equals(gridId)) {
            envServices = envServiceRepository.findCrossRegion();
        } else {
            envServices = envServiceRepository.findByGridId(gridId);
        }
        return envServices
                .stream()
                .map(EnvService::getServiceJson)
                .toList();
    }

    /**
     * 获取环境级服务名称列表
     * */
    public List<String> getEnvServiceNameList(String gridId) {
        List<EnvService> envServices;
        if("crossRegion".equals(gridId)) {
            envServices = envServiceRepository.findCrossRegion();
        } else {
            envServices = envServiceRepository.findByGridId(gridId);
        }
        return envServices
                .stream()
                .map(EnvService::getServiceName)
                .toList();
    }

    /**
     * 获取环境级服务列表
     * */
    public List<EnvService> getEnvServiceList(String gridId) {
        if("crossRegion".equals(gridId)) {
            return envServiceRepository.findCrossRegion();
        }
        return  envServiceRepository.findByGridId(gridId);
    }

}
