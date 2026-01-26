package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.EventFusionDeployDetail;
import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import edu.fudan.se.sctap_lowcode_tool.model.EnvEventGrid;
import edu.fudan.se.sctap_lowcode_tool.model.GridMesh;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvEventGridRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvEventRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.GridMeshRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EnvEventService {
    @Resource
    private EnvEventRepository envEventRepository;

    @Resource
    private EnvEventGridRepository envEventGridRepository;

    @Resource
    private GridMeshRepository gridMeshRepository;

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

    /**
     * 获取事件融合部署详情
     * @param envEventId 环境级事件ID
     * @return 部署详情列表
     */
    public List<EventFusionDeployDetail> getEventFusionDeployDetail(Integer envEventId) {
        List<EnvEventGrid> envEventGridList = envEventGridRepository.findByEnvEventId(envEventId);
        return envEventGridList.stream().map(envEventGrid -> {
            EventFusionDeployDetail detail = new EventFusionDeployDetail();
            detail.setGridId(envEventGrid.getGridId());
            GridMesh gridMesh = gridMeshRepository.findById(envEventGrid.getGridId()).orElse(null);
            if(gridMesh != null) {
                detail.setMeshNo(gridMesh.getMeshNo());
                detail.setMeshName(gridMesh.getMeshName());
            }
            return detail;
        }).collect(Collectors.toList());
    }

    /**
     * 删除环境级事件
     * @param envEventId 环境级事件ID
     */
    public void deleteEnvEvent(Integer envEventId) {
        // 1. 删除env_event_grid表中的关联记录
        List<EnvEventGrid> envEventGridList = envEventGridRepository.findByEnvEventId(envEventId);
        if (!envEventGridList.isEmpty()) {
            envEventGridRepository.deleteAll(envEventGridList);
            log.info("删除了 {} 条env_event_grid关联记录", envEventGridList.size());
        }
        
        // 2. 删除env_event表中的记录
        envEventRepository.deleteById(envEventId);
        log.info("删除了env_event记录，ID: {}", envEventId);
    }

}
