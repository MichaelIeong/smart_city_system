package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.EventFusionDeployDetail;
import edu.fudan.se.sctap_lowcode_tool.DTO.EventFusionSyncRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.EventFusionSyncResponse;
import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.constant.RoleConstant;
import edu.fudan.se.sctap_lowcode_tool.model.*;
import edu.fudan.se.sctap_lowcode_tool.repository.EdgeNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvEventGridRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvEventRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.GridMeshRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProductService productService;

    @Value("${app.node-role:edge}")
    private String nodeRole;

    @Autowired
    private EdgeNodeRepository edgeNodeRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public List<EnvEvent> getEnvEventListByProject(String gridId, Integer projectId) {
        // 获取跨区域事件时，增加 projectId 过滤
        if ("crossRegion".equals(gridId)) {
            return envEventRepository.findCrossRegionByProject(projectId);
        }
        return envEventRepository.findByGridId(gridId);
    }

    /**
     * 根据projectId获取全部环境级事件
     * */
    public List<EnvEvent> getEnvEventListByProjectId(Integer projectId) {
        return envEventRepository.findByProjectId(projectId);
    }

    /**
     * 分页查询环境级事件
     * */
    public PageDTO<EnvEvent> list(String eventType, String eventName, Integer projectId, int pageNo, int pageSize, String sortField, String sortOrder) {
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
                projectId,
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
            if(RoleConstant.CLOUD.equals(nodeRole)) {
                for (EnvEventGrid grid : envEventGridList) {
                    EdgeNode targetNode = edgeNodeRepository.findByGridId(grid.getGridId());
                    if (targetNode != null) {
                        dispatchDeleteToEdge(targetNode.getIpAddress(), envEventId);
                    } else {
                        log.warn("未找到 gridId = {} 对应的边缘节点，跳过删除下发", grid.getGridId());
                    }
                }
            }
        }
        
        // 2. 删除env_event表中的记录
        envEventRepository.deleteById(envEventId);
        log.info("删除了env_event记录，ID: {}", envEventId);
    }

    /**
     * 将删除指令下发至指定的边缘节点
     */
    private void dispatchDeleteToEdge(String ipAddress, Integer envEventId) {
        String url = ipAddress + "/api/envEvent/{id}";
        try {
            // 使用 restTemplate 发送 DELETE 请求，自动替换 URL 中的占位符 {id}
            restTemplate.delete(url, envEventId);
            log.info("边缘节点 [{}] 环境级事件 [{}] 删除下发成功", ipAddress, envEventId);
        } catch (Exception e) {
            // 捕获异常，防止某一个边端断网导致云端的删除事务全部失败/回滚
            log.error("向边缘节点 [{}] 下发环境级事件 [{}] 删除请求失败: {}", ipAddress, envEventId, e.getMessage());
        }
    }

    /**
     * 获取同类型的网格列表
     * @param eventId 事件ID
     * @return 同类型网格列表
     */
    public List<GridMesh> getGridListByEventId(Integer eventId) {
        // 1. 获取该事件的一个已部署网格id
        List<EnvEventGrid> envEventGrids = envEventGridRepository.findByEnvEventId(eventId);
        if(envEventGrids.isEmpty()) {
            return List.of();
        }
        String gridId = envEventGrids.get(0).getGridId();
        
        // 2. 获取该网格信息
        GridMesh refGrid = gridMeshRepository.findById(gridId).orElse(null);
        if(refGrid == null) {
            return List.of();
        }
        
        // 3. 查找所有mesh_nature和mesh_type都相同的网格
        String sql = "SELECT * FROM grid_list WHERE mesh_nature = ? AND mesh_type = ?";
        List<GridMesh> allGrids = jdbcTemplate.query(sql, 
            (rs, rowNum) -> {
                GridMesh grid = new GridMesh();
                grid.setId(rs.getString("id"));
                grid.setMeshNo(rs.getString("mesh_no"));
                grid.setMeshName(rs.getString("mesh_name"));
                grid.setMeshNature(rs.getString("mesh_nature"));
                grid.setMeshArea(rs.getDouble("mesh_area"));
                grid.setMeshType(rs.getString("mesh_type"));
                return grid;
            },
            refGrid.getMeshNature(), refGrid.getMeshType());
        
        return allGrids;
    }

    /**
     * 应用同步
     * @param request 同步请求
     * @return 同步结果列表
     */
    public List<EventFusionSyncResponse> syncEventFusion(EventFusionSyncRequest request) {
        List<EventFusionSyncResponse> responses = new ArrayList<>();
        Integer eventId = request.getEventId();
        List<String> gridIdList = request.getGridIdList();
        
        // 获取事件信息
        EnvEvent envEvent = envEventRepository.findById(eventId).orElse(null);
        if(envEvent == null) {
            gridIdList.forEach(gridId -> {
                GridMesh gridMesh = gridMeshRepository.findById(gridId).orElse(null);
                responses.add(new EventFusionSyncResponse(
                    gridId,
                    gridMesh != null ? gridMesh.getMeshNo() : null,
                    gridMesh != null ? gridMesh.getMeshName() : null,
                    0,
                    "事件不存在"
                ));
            });
            return responses;
        }
        
        // 处理每个网格
        for(String gridId : gridIdList) {
            responses.add(checkAndSyncEventToGrid(gridId, eventId, envEvent));
        }
        
        return responses;
    }

    /**
     * 检查网格并执行同步
     */
    private EventFusionSyncResponse checkAndSyncEventToGrid(String gridId, Integer eventId, EnvEvent envEvent) {
        GridMesh gridMesh = gridMeshRepository.findById(gridId).orElse(null);
        if(gridMesh == null) {
            return new EventFusionSyncResponse(gridId, null, null, 0, "网格不存在");
        }
        
        // 判断是否已经存在
        List<EnvEventGrid> existing = envEventGridRepository.findByEnvEventId(eventId);
        boolean alreadyDeployed = existing.stream().anyMatch(e -> e.getGridId().equals(gridId));
        if (alreadyDeployed) {
            return new EventFusionSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 1, "该网格已部署本事件");
        }
        
        // 检查硬件依赖
        List<String> dependDtypes = envEvent.getDependDtypes();
        if(dependDtypes != null && !dependDtypes.isEmpty()) {
            // 使用ProductService获取网格的设备类型
            List<Map<String, String>> gridDeviceTypeList = productService.getDeviceTypesByGridId(gridId);
            List<String> gridDeviceTypes = gridDeviceTypeList.stream()
                .map(map -> map.get("value"))
                .collect(Collectors.toList());
            
            // 检查是否满足所有依赖
            for(String requiredType : dependDtypes) {
                if(!gridDeviceTypes.contains(requiredType)) {
                    return new EventFusionSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 
                        0, "硬件不满足：缺少设备类型 " + requiredType);
                }
            }
        }
        
        // 创建关联记录
        EnvEventGrid envEventGrid = new EnvEventGrid();
        envEventGrid.setGridId(gridId);
        envEventGrid.setEnvEventId(eventId);
        envEventGrid.setEnabled(true);
        envEventGridRepository.save(envEventGrid);

        // ================= 新增：云端下发到边端服务器 =================
        if (RoleConstant.CLOUD.equals(nodeRole)) {
            boolean dispatchSuccess = dispatchAddEventToEdge(envEvent, gridId);
            if (!dispatchSuccess) {
                // 如果下发失败，回滚本地的关联表记录，避免云边状态不一致
                envEventGridRepository.delete(envEventGrid);
                return new EventFusionSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 0, "下发边缘节点失败，网络异常或节点未响应");
            }
        }
        
        return new EventFusionSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 1, "同步成功");
    }

    /**
     * 将新增/同步请求下发至指定的边缘节点 (共用方法)
     * 返回 boolean 用于判断是否下发成功，以便上层决定是否回滚
     */
    private boolean dispatchAddEventToEdge(EnvEvent envEvent, String gridId) {
        EdgeNode targetNode = edgeNodeRepository.findByGridId(gridId);
        if (targetNode == null) {
            log.warn("未找到 gridId = {} 对应的边缘节点，跳过下发。", gridId);
            return false;
        }
        String ipAddress = targetNode.getIpAddress();
        // 复用带 id 参数的 /add 接口
        String url = ipAddress + "/api/envEvent/add?gridId={gridId}";
        try {
            restTemplate.postForEntity(
                    url,
                    envEvent,
                    Integer.class,
                    gridId
            );
            log.info("边缘节点 [{}] 事件下发成功", ipAddress);
            return true;
        } catch (Exception e) {
            log.error("向边缘节点 [{}] 下发事件失败: {}", ipAddress, e.getMessage());
            return false;
        }
    }

}
