package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvServiceRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    /**
     * 获取全部环境级服务
     * */
    public List<EnvService> getAllEnvServiceList() {
        return envServiceRepository.findAll();
    }

    /**
     * 分页查询环境级服务
     * */
    public PageDTO<EnvService> list(String name, String description, int pageNo, int pageSize, String sortField, String sortOrder) {
        // 1. 动态创建 Sort 对象
        Sort sort;
        if (sortField != null && !sortField.isEmpty()) {
            Sort.Direction direction = Sort.Direction.ASC;
            if ("descend".equals(sortOrder)) {
                direction = Sort.Direction.DESC;
            }
            sort = Sort.by(direction, sortField);
        } else {
            sort = Sort.by("id").ascending();
        }
        // 2. 使用动态创建的 sort 对象
        Pageable pageable = PageRequest.of(
                pageNo - 1,
                pageSize,
                sort
        );
        // 3. 执行查询
        Page<EnvService> repoResult = envServiceRepository.searchWithFilters(
                name,
                description,
                pageable
        );
        // 4. 返回结果
        return new PageDTO<>(
                pageNo, pageSize,
                repoResult.getTotalElements(), repoResult.getTotalPages(),
                repoResult.getContent()
        );
    }

}
