package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    public List<ServiceBriefResponse> findAllByProjectId(Integer projectId) {
        return serviceRepository.findAllByProjectId(projectId).stream().map(ServiceBriefResponse::new).toList();
    }

}
