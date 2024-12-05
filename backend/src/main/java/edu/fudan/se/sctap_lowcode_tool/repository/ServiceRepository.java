package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.ServiceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<ServiceInfo, Integer> {

    @Query("SELECT s FROM ServiceInfo s JOIN s.parentingSpace p WHERE p.projectInfo.projectId = :projectId")
    List<ServiceInfo> findAllByProjectId(@Param("projectId") Integer projectId);

    @Query("SELECT s " +
            "FROM ServiceInfo s " +
            "JOIN SpaceInfo sp ON s.parentingSpace = sp " +
            "WHERE s.serviceId = :serviceId " +
            "AND sp.spaceId = :spaceId " +
            "AND sp.projectInfo.projectId = :projectId")
    Optional<ServiceInfo> findSpecificService(@Param("serviceId") String serviceId,
                                              @Param("spaceId") String spaceId,
                                              @Param("projectId") Integer projectId);

}
