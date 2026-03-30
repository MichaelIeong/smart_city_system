package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.constant.RoleConstant;
import edu.fudan.se.sctap_lowcode_tool.model.EdgeNode;
import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.*;
import edu.fudan.se.sctap_lowcode_tool.utils.JsonUtil;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.MilvusUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private JsonUtil jsonUtil;


    @Resource
    private TslDeviceRepository tslDeviceRepository;

    @Resource
    private EnvEventRepository envEventRepository;

    @Resource
    private EnvEventGridRepository envEventGridRepository;

    @Resource
    private EnvServiceRepository envServiceRepository;

    @Resource
    private EnvServiceGridRepository envServiceGridRepository;

    @Resource
    private AppRuleRepository appRuleRepository;

    @Resource
    private AppGridRepository appGridRepository;

    @Resource
    private MilvusUtil milvusUtil;

    @Value("${app.node-role:edge}")
    private String nodeRole;

    @Resource
    private EdgeNodeRepository edgeNodeRepository;

    @Resource
    private RestTemplate restTemplate;

    // 映射 projectId 到 mesh_nature
    private static final Map<Integer, String> MESH_NATURE_MAP = new HashMap<>();
    static {
        MESH_NATURE_MAP.put(1, "F-city");
        MESH_NATURE_MAP.put(2, "F-community");
        MESH_NATURE_MAP.put(3, "F-park");
    }

    /**
     * 保存或更新项目。
     *
     * @param projectInfo 项目信息
     * @return 保存或更新后的项目信息
     */
    public ProjectInfo saveOrUpdateProject(ProjectInfo projectInfo) {
        return projectRepository.save(projectInfo);
    }

    /**
     * 根据ID删除项目。
     *
     * @param projectId 项目ID
     * @return 删除成功返回true，否则返回false
     */
    public boolean deleteProjectById(int projectId) {
        if (projectRepository.existsById(projectId)) {
            projectRepository.deleteById(projectId);
            return true;
        }
        return false;
    }

    /**
     * 根据ID查找项目。
     *
     * @param projectId 项目ID
     * @return 项目信息
     */
    public Optional<ProjectInfo> findById(int projectId) {
        return Optional.ofNullable(projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found")));
    }

    /**
     * 根据ID获取项目名称。
     *
     * @param projectId 项目ID
     * @return 项目名称
     */
    public String getProjectName(int projectId) {
        return projectRepository.findById(projectId)
                .map(ProjectInfo::getProjectName)
                .orElse("Project not found");
    }

    /**
     * 获取所有项目。
     *
     * @return 所有项目信息
     */
    public Iterable<ProjectInfo> findAll() {
        return Optional.of(projectRepository.findAll())
                .orElseGet(Collections::emptyList);
    }

    /**
     * 导入项目（从JSON字符串）。
     *
     * @param json 项目JSON字符串
     * @return 导入成功返回true，否则返回false
     */
    public boolean importProjects(String json) {
        return Optional.ofNullable(json)
                .map(j -> jsonUtil.parseJsonToList(j, ProjectInfo.class))
                .map(projects -> {
                    projects.forEach(this::saveOrUpdateProject);
                    return true;
                })
                .orElse(false);
    }

    /**
     * 导出所有项目为JSON字符串。
     *
     * @return JSON字符串
     */
    public Optional<String> exportProjects() {
        return Optional.ofNullable(findAll())
                .map(projects -> jsonUtil.convertListToJson((List<ProjectInfo>) projects));
    }

    // 开启事务，任何异常都回滚
    @Transactional(transactionManager = "jpaTransactionManager", propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Boolean deleteProjectById(Integer projectId) {
        try {
            // 1. 删除设备
            String meshNature = MESH_NATURE_MAP.get(projectId);
            if(meshNature != null) {
                tslDeviceRepository.deleteByMeshNature(meshNature);
            }
            // 2. 删除环境级事件及其关联 Grid
            List<Integer> eventIds = envEventRepository.findIdsByProjectId(projectId);
            if (!eventIds.isEmpty()) {
                envEventGridRepository.deleteByEnvEventIdIn(eventIds); // 先删子表
                envEventRepository.deleteByProjectId(projectId);       // 后删主表
            }
            // 3. 删除环境级服务及其关联 Grid
            List<Integer> serviceIds = envServiceRepository.findIdsByProjectId(projectId);
            if (!serviceIds.isEmpty()) {
                envServiceGridRepository.deleteByEnvServiceIdIn(serviceIds);
                envServiceRepository.deleteByProjectId(projectId);
            }
            // 4. 删除应用级规则及其关联 Grid
            List<Integer> appRuleIds = appRuleRepository.findIdsByProjectId(projectId);
            if (!appRuleIds.isEmpty()) {
                appGridRepository.deleteByAppRuleIdIn(appRuleIds);
                appRuleRepository.deleteByProjectId(projectId);
                // 如果是云端节点，删除向量数据库
                if(RoleConstant.CLOUD.equals(nodeRole)) {
                    for(Integer appRuleId : appRuleIds) {
                        milvusUtil.deleteRecordById(appRuleId.toString());
                    }
                }
            }
            // 5. 向边端下发删除请求
            if(RoleConstant.CLOUD.equals(nodeRole)) {
                notifyEdgeNodesToDelete(projectId);
            }
            return true;
        } catch (Exception e) {
            log.error("删除项目失败，触发事务回滚. projectId: {}", projectId, e);
            // 重要：必须抛出运行时异常，@Transactional 才会生效回滚
            throw new RuntimeException("Delete project failed, rolling back...", e);
        }
    }

    /**
     * 向所有记录在案的边缘节点下发删除指令
     */
    private void notifyEdgeNodesToDelete(Integer projectId) {
        List<EdgeNode> edgeNodes = edgeNodeRepository.findAll();
        if (edgeNodes.isEmpty()) {
            log.info("没有找到边缘节点，跳过下发删除指令。");
            return;
        }
        for (EdgeNode node : edgeNodes) {
            try{
                restTemplate.getForEntity(node.getIpAddress() + "/api/projects/delete/" + projectId, Boolean.class);
                log.info("边缘节点 [{}] 场景删除成功", node.getIpAddress());
            } catch (Exception e) {
                log.error("请求边缘节点 [{}] 场景删除失败，可能网络不通或服务异常: {}", node.getIpAddress(), e.getMessage());
            }
        }
    }
}



























