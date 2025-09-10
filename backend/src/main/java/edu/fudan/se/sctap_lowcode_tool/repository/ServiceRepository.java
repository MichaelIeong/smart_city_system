package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.ServiceInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<ServiceInfo, Integer> {

    // 按 projectId 查
    List<ServiceInfo> findAllByProjectId(String projectId);

    // 按 serviceId 查
    ServiceInfo findByServiceId(Integer serviceId);
}