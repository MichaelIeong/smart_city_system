package edu.fudan.se.sctap_lowcode_tool.repository;


import edu.fudan.se.sctap_lowcode_tool.model.DeviceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DeviceHistoryRepository extends JpaRepository<DeviceHistory, Integer> {
    Set<DeviceHistory> findAllByDeviceId(int deviceId);
}
