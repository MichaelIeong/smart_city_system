package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.*;
import edu.fudan.se.sctap_lowcode_tool.constant.RoleConstant;
import edu.fudan.se.sctap_lowcode_tool.model.*;
import edu.fudan.se.sctap_lowcode_tool.repository.EdgeNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvServiceGridRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvServiceRepository;
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
public class EnvServiceService {
    @Resource
    private EnvServiceRepository envServiceRepository;

    @Resource
    private EnvServiceGridRepository envServiceGridRepository;

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
     * 获取环境级服务列表 (支持项目过滤)
     * */
    public List<EnvService> getEnvServiceListByProject(String gridId, Integer projectId) {
        if ("crossRegion".equals(gridId)) {
            return envServiceRepository.findCrossRegionByProject(projectId);
        }
        return envServiceRepository.findByGridId(gridId);
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
    public PageDTO<EnvService> list(String name, String description, Integer projectId, int pageNo, int pageSize, String sortField, String sortOrder) {
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
     * 获取服务组部署详情
     * @param envServiceId 环境级服务ID
     * @return 部署详情列表
     */
    public List<ServiceGroupDeployDetail> getServiceGroupDeployDetail(Integer envServiceId) {
        List<EnvServiceGrid> envServiceGridList = envServiceGridRepository.findByEnvServiceId(envServiceId);
        return envServiceGridList.stream().map(envServiceGrid -> {
            ServiceGroupDeployDetail detail = new ServiceGroupDeployDetail();
            detail.setGridId(envServiceGrid.getGridId());
            GridMesh gridMesh = gridMeshRepository.findById(envServiceGrid.getGridId()).orElse(null);
            if(gridMesh != null) {
                detail.setMeshNo(gridMesh.getMeshNo());
                detail.setMeshName(gridMesh.getMeshName());
            }
            return detail;
        }).collect(Collectors.toList());
    }

    /**
     * 删除环境级服务
     * @param envServiceId 环境级服务ID
     */
    public void deleteEnvService(Integer envServiceId) {
        // 1. 删除env_service_grid表中的关联记录
        List<EnvServiceGrid> envServiceGridList = envServiceGridRepository.findByEnvServiceId(envServiceId);
        if (!envServiceGridList.isEmpty()) {
            envServiceGridRepository.deleteAll(envServiceGridList);
            log.info("删除了 {} 条env_service_grid关联记录", envServiceGridList.size());
            // 【特定区域】：根据关联表找到对应的特定边缘节点并下发删除请求
            for (EnvServiceGrid grid : envServiceGridList) {
                EdgeNode targetNode = edgeNodeRepository.findByGridId(grid.getGridId());
                if (targetNode != null) {
                    dispatchDeleteToEdge(targetNode.getIpAddress(), envServiceId);
                } else {
                    log.warn("未找到 gridId = {} 对应的边缘节点，跳过删除下发", grid.getGridId());
                }
            }
        }
        // 2. 删除env_service表中的记录
        envServiceRepository.deleteById(envServiceId);
        log.info("删除了env_service记录，ID: {}", envServiceId);
    }

    /**
     * 将删除指令下发至指定的边缘节点
     */
    private void dispatchDeleteToEdge(String ipAddress, Integer envServiceId) {
        // 请确保 /api/envService/{id} 与你 EnvServiceController 中的 @RequestMapping 路径一致
        String url = ipAddress + "/api/envService/{id}";
        try {
            // 使用 restTemplate 发送 DELETE 请求，自动替换 URL 中的占位符 {id}
            restTemplate.delete(url, envServiceId);
            log.info("边缘节点 [{}] 环境级服务 [{}] 删除下发成功", ipAddress, envServiceId);
        } catch (Exception e) {
            // 捕获异常，防止某一个边端断网导致云端的删除事务全部失败/回滚
            log.error("向边缘节点 [{}] 下发环境级服务 [{}] 删除请求失败: {}", ipAddress, envServiceId, e.getMessage());
        }
    }

    /**
     * 获取同类型的网格列表
     * @param serviceId 服务ID
     * @return 同类型网格列表
     */
    public List<GridMesh> getGridListByServiceId(Integer serviceId) {
        // 1. 获取该服务的一个已部署网格id
        List<EnvServiceGrid> envServiceGrids = envServiceGridRepository.findByEnvServiceId(serviceId);
        if(envServiceGrids.isEmpty()) {
            return List.of();
        }
        String gridId = envServiceGrids.get(0).getGridId();
        
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
    public List<ServiceGroupSyncResponse> syncServiceGroup(ServiceGroupSyncRequest request) {
        List<ServiceGroupSyncResponse> responses = new ArrayList<>();
        Integer serviceId = request.getServiceId();
        List<String> gridIdList = request.getGridIdList();
        
        // 获取服务信息
        EnvService envService = envServiceRepository.findById(serviceId).orElse(null);
        if(envService == null) {
            gridIdList.forEach(gridId -> {
                GridMesh gridMesh = gridMeshRepository.findById(gridId).orElse(null);
                responses.add(new ServiceGroupSyncResponse(
                    gridId,
                    gridMesh != null ? gridMesh.getMeshNo() : null,
                    gridMesh != null ? gridMesh.getMeshName() : null,
                    0,
                    "服务不存在"
                ));
            });
            return responses;
        }
        
        // 处理每个网格
        for(String gridId : gridIdList) {
            responses.add(checkAndSyncServiceToGrid(gridId, serviceId, envService));
        }
        
        return responses;
    }

    /**
     * 检查网格并执行同步
     */
    private ServiceGroupSyncResponse checkAndSyncServiceToGrid(String gridId, Integer serviceId, EnvService envService) {
        GridMesh gridMesh = gridMeshRepository.findById(gridId).orElse(null);
        if(gridMesh == null) {
            return new ServiceGroupSyncResponse(gridId, null, null, 0, "网格不存在");
        }
        
        // 判断是否已经存在
        List<EnvServiceGrid> existing = envServiceGridRepository.findByEnvServiceId(serviceId);
        boolean alreadyDeployed = existing.stream().anyMatch(e -> e.getGridId().equals(gridId));
        if (alreadyDeployed) {
            return new ServiceGroupSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 1, "该网格已部署本服务");
        }
        
        // 检查硬件依赖
        List<String> dependDtypes = envService.getDependDtypes();
        if(dependDtypes != null && !dependDtypes.isEmpty()) {
            // 使用ProductService获取网格的设备类型
            List<Map<String, String>> gridDeviceTypeList = productService.getDeviceTypesByGridId(gridId);
            List<String> gridDeviceTypes = gridDeviceTypeList.stream()
                .map(map -> map.get("value"))
                .collect(Collectors.toList());
            
            // 检查是否满足所有依赖
            for(String requiredType : dependDtypes) {
                if(!gridDeviceTypes.contains(requiredType)) {
                    return new ServiceGroupSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 
                        0, "硬件不满足：缺少设备类型 " + requiredType);
                }
            }
        }
        
        // 创建关联记录
        EnvServiceGrid envServiceGrid = new EnvServiceGrid();
        envServiceGrid.setGridId(gridId);
        envServiceGrid.setEnvServiceId(serviceId);
        envServiceGrid.setEnabled(true);
        envServiceGridRepository.save(envServiceGrid);

        // ================= 新增：云端下发到边端服务器 =================
        if (RoleConstant.CLOUD.equals(nodeRole)) {
            boolean dispatchSuccess = dispatchAddServiceToEdge(envService, gridId);
            if (!dispatchSuccess) {
                // 如果下发失败，回滚本地的关联表记录，避免云边状态不一致
                envServiceGridRepository.delete(envServiceGrid);
                return new ServiceGroupSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 0, "下发边缘节点失败，网络异常或节点未响应");
            }
        }
        
        return new ServiceGroupSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 1, "同步成功");
    }

    /**
     * 将新增/同步请求下发至指定的边缘节点 (共用方法)
     * 返回 boolean 用于判断是否下发成功，以便上层决定是否回滚
     */
    private boolean dispatchAddServiceToEdge(EnvService envService, String gridId) {
        EdgeNode targetNode = edgeNodeRepository.findByGridId(gridId);
        if (targetNode == null) {
            log.warn("未找到 gridId = {} 对应的边缘节点，跳过下发。", gridId);
            return false;
        }
        String ipAddress = targetNode.getIpAddress();
        // 复用带 id 参数的 /add 接口
        String url = ipAddress + "/api/envService/add?gridId={gridId}&id={id}";
        try {
            restTemplate.postForEntity(
                    url,
                    envService,
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
