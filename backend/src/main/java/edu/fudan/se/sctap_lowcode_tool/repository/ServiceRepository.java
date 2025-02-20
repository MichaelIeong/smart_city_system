package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.ServiceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceRepository extends JpaRepository<ServiceInfo, Integer> {

    @Query("SELECT s FROM ServiceInfo s JOIN s.parentingSpace p WHERE p.projectInfo.projectId = :projectId")
    List<ServiceInfo> findAllByProjectId(@Param("projectId") Integer projectId);

    ServiceInfo findByServiceId(String serviceId);
}
