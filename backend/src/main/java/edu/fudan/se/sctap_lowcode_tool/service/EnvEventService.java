package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvEventRepository;
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
public class EnvEventService {
    @Resource
    private EnvEventRepository envEventRepository;

    /**
     * 获取环境级事件列表
     * */
    public List<String> getEnvEventJsonList(String gridId) {
        List<EnvEvent> envEvents;
        // 获取跨区域事件
        if("crossRegion".equals(gridId)) {
            envEvents = envEventRepository.findCrossRegion();
        } else {
            envEvents = envEventRepository.findByGridId(gridId);
        }
        return envEvents
                .stream()
                .map(EnvEvent::getEventJson)
                .toList();
    }

    /**
     * 获取环境级事件类型列表
     * */
    public List<String> getEnvEventTypeList(String gridId) {
        List<EnvEvent> envEvents;
        // 获取跨区域事件
        if("crossRegion".equals(gridId)) {
            envEvents = envEventRepository.findCrossRegion();
        } else {
            envEvents = envEventRepository.findByGridId(gridId);
        }
        return envEvents
                .stream()
                .map(EnvEvent::getEventType)
                .toList();
    }

    /**
     * 获取环境级事件列表
     * */
    public List<EnvEvent> getEnvEventList(String gridId) {
        // 获取跨区域事件
        if("crossRegion".equals(gridId)) {
            return envEventRepository.findCrossRegion();
        }
        return envEventRepository.findByGridId(gridId);
    }

    /**
     * 获取全部环境级事件
     * */
    public List<EnvEvent> getAllEnvEventList() {
        return envEventRepository.findAll();
    }

    /**
     * 分页查询环境级事件
     * */
    public PageDTO<EnvEvent> list(String eventType, String eventName, int pageNo, int pageSize, String sortField, String sortOrder) {
        // 1. 动态创建 Sort 对象
        Sort sort;
        if (sortField != null && !sortField.isEmpty()) {
            // 映射排序方向
            Sort.Direction direction = Sort.Direction.ASC;
            if ("descend".equals(sortOrder)) {
                direction = Sort.Direction.DESC;
            }
            sort = Sort.by(direction, sortField);
        } else {
            // 如果没有排序字段，默认按 id 升序
            sort = Sort.by("id").ascending();
        }
        // 2. 使用动态创建的 sort 对象
        Pageable pageable = PageRequest.of(
                pageNo - 1,
                pageSize,
                sort
        );
        // 3. 执行查询
        Page<EnvEvent> repoResult = envEventRepository.searchWithFilters(
                eventType,
                eventName,
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
