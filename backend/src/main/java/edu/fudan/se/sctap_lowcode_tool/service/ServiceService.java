package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.model.FusionRule;
import edu.fudan.se.sctap_lowcode_tool.model.ServiceInfo;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.ServiceNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.ServiceNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import edu.fudan.se.sctap_lowcode_tool.util.ServiceConverterUtil;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private ServiceNodeRepository serviceNodeRepository;

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
        ServiceNode node = ServiceConverterUtil.convertToNode(serviceInfo, neo4jServiceId);

// 保存到 Neo4j
        serviceNodeRepository.save(node);
    }

    public ServiceInfo getService(Integer serviceId){
        return serviceRepository.findByServiceId(serviceId);
    }

}
