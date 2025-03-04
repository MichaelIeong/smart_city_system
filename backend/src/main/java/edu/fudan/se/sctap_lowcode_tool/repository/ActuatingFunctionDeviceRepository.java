package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.ActuatingFunctionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActuatingFunctionDeviceRepository extends JpaRepository<ActuatingFunctionInfo, Integer> {

    // 使用 JPQL 查询 url 字段
    @Query("SELECT a.url FROM ActuatingFunctionDevice a WHERE a.actuatingFunction.id = :serviceId AND a.device.deviceName = :name")
    String findUrlByServiceIdAndName(@Param("serviceId") Integer serviceId, @Param("name") String name);
}
