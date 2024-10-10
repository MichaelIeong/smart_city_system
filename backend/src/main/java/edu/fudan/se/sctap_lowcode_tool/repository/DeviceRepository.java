package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceInfo, Integer> {
    Optional<DeviceInfo> findById(int id);

    @Query("SELECT d FROM DeviceInfo d JOIN d.space s WHERE s.projectInfo.projectId = :projectId")
    List<DeviceInfo> findAllByProjectId(@Param("projectId") Integer projectId);

}