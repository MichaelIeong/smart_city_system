package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.PageDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceGroupDeployDetail;
import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceGroupSyncRequest;
import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceGroupSyncResponse;
import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import edu.fudan.se.sctap_lowcode_tool.model.EnvServiceGrid;
import edu.fudan.se.sctap_lowcode_tool.model.GridMesh;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvServiceGridRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvServiceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.GridMeshRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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
        }
        
        // 2. 删除env_service表中的记录
        envServiceRepository.deleteById(envServiceId);
        log.info("删除了env_service记录，ID: {}", envServiceId);
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
        String dependDtypes = envService.getDependDtypes();
        if(dependDtypes != null && !dependDtypes.trim().isEmpty() && !dependDtypes.equals("[]")) {
            try {
                List<String> requiredDeviceTypes = objectMapper.readValue(dependDtypes, new TypeReference<List<String>>(){});
                
                // 使用ProductService获取网格的设备类型
                List<Map<String, String>> gridDeviceTypeList = productService.getDeviceTypesByGridId(gridId);
                List<String> gridDeviceTypes = gridDeviceTypeList.stream()
                    .map(map -> map.get("value"))
                    .collect(Collectors.toList());
                
                // 检查是否满足所有依赖
                for(String requiredType : requiredDeviceTypes) {
                    if(!gridDeviceTypes.contains(requiredType)) {
                        return new ServiceGroupSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 
                            0, "硬件不满足：缺少设备类型 " + requiredType);
                    }
                }
            } catch (Exception e) {
                log.error("解析depend_dtypes失败", e);
                return new ServiceGroupSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 
                    0, "硬件依赖检查失败：" + e.getMessage());
            }
        }
        
        // 创建关联记录
        EnvServiceGrid envServiceGrid = new EnvServiceGrid();
        envServiceGrid.setGridId(gridId);
        envServiceGrid.setEnvServiceId(serviceId);
        envServiceGrid.setEnabled(true);
        envServiceGridRepository.save(envServiceGrid);
        
        return new ServiceGroupSyncResponse(gridId, gridMesh.getMeshNo(), gridMesh.getMeshName(), 1, "同步成功");
    }

}
