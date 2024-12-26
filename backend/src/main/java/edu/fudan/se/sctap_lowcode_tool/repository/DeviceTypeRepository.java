package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceTypeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTypeRepository extends JpaRepository<DeviceTypeInfo, Integer> {
    boolean existsByDeviceTypeName(String deviceTypeName);

    Optional<DeviceTypeInfo> findById(int id);

    List<DeviceTypeInfo> findByProjectInfoProjectId(int projectId);

}