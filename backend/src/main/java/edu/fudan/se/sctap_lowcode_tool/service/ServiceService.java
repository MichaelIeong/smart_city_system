package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.execution.TaskScheduler;
import edu.fudan.se.sctap_lowcode_tool.execution.WorkflowParser;
import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;
import edu.fudan.se.sctap_lowcode_tool.model.ServiceInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.ServiceNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.ServiceNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvServiceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import edu.fudan.se.sctap_lowcode_tool.utils.ServiceConverterUtil;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private ServiceNodeRepository serviceNodeRepository;

    @Autowired
    private EnvServiceRepository envServiceRepository;

    @Autowired
    private WorkflowParser parser;

    @Autowired
    private TaskScheduler scheduler;
    public List<ServiceBriefResponse> findAllByProjectId(String projectId) {
        return serviceRepository.findAllByProjectId(projectId).stream().map(ServiceBriefResponse::new).toList();
    }

    /**
     * 获取所有服务的列表。
     *
     * @return 数据库中所有服务的列表
     */
    public List<ServiceInfo> getServiceList() {
        return serviceRepository.findAll();
    }
    public List<ServiceNode> getServiceNodeList() {
        return serviceNodeRepository.findAll();
    }


    public void addOrUpdateService(ServiceInfo serviceInfo){

        serviceRepository.save(serviceInfo);
        Integer maxId = serviceNodeRepository.findMaxServiceId();
        int neo4jServiceId = (maxId != null ? maxId + 1 : 1);

// 转换实体
//        ServiceNode node = ServiceConverterUtil.convertToNode(serviceInfo, neo4jServiceId);
//
//// 保存到 Neo4j
//        serviceNodeRepository.save(node);
    }

    public List<ServiceInfo> getServiceListByProjectId(String projectId) {
        return serviceRepository.findAllByProjectId(projectId);
    }

    public ServiceInfo getService(Integer serviceId){
        return serviceRepository.findByServiceId(serviceId);
    }


    public void executeServiceById(Integer serviceId) throws Exception {
        // 1. 获取服务 JSON
        ServiceInfo serviceInfo = getService(serviceId);
        String serviceJson = serviceInfo.getServiceJson();

        // 2. 初始化工作流解析器
        parser.initParser(serviceJson);

        // 3. 执行工作流
        scheduler.start(parser.getStartNodeId());
    }

    public void saveCompositionService(EnvService envService){
        envServiceRepository.save(envService);
    }

}
