package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceTypeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceTypeRepository extends JpaRepository<DeviceTypeInfo, Integer> {
    boolean existsByDeviceTypeName(String deviceTypeName);
}