package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.TslDevices;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TslDeviceRepository extends JpaRepository<TslDevices, String> {
}
