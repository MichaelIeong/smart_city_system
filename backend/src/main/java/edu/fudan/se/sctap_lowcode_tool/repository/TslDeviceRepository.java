package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeSummaryDTO;
import edu.fudan.se.sctap_lowcode_tool.model.TslDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TslDeviceRepository extends JpaRepository<TslDevice, Long> {

    // 1. [新功能] 全局聚合：根据场景(meshNature)统计
    @Query("SELECT new edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeSummaryDTO(" +
            "d.product.productName, d.product.productFunction, COUNT(d)) " +
            "FROM TslDevice d WHERE d.meshNature = :sceneType " +
            "GROUP BY d.product.productId, d.product.productName, d.product.productFunction " +
            "ORDER BY COUNT(d) DESC")
    List<DeviceTypeSummaryDTO> findGlobalSummaryByScene(@Param("sceneType") String sceneType);

    // 2. [新功能] 网格聚合：根据网格编号(meshNo)统计
    @Query("SELECT new edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeSummaryDTO(" +
            "d.product.productName, d.product.productFunction, COUNT(d)) " +
            "FROM TslDevice d WHERE d.meshNo = :gridId " +
            "GROUP BY d.product.productId, d.product.productName, d.product.productFunction " +
            "ORDER BY COUNT(d) DESC")
    List<DeviceTypeSummaryDTO> findGridSummaryByGridId(@Param("gridId") String gridId);

    // 3. [原有功能重构] 根据产品ID查询设备实例列表
    // 对应原本的 queryDeviceInstances 功能
    List<TslDevice> findByProductProductId(String productId);

    // 4. [新功能] 根据 product_id 和 mesh_id 统计数量
    @Query("SELECT COUNT(d) FROM TslDevice d WHERE d.product.productId = :productId AND d.meshId = :meshId")
    long countByProductAndMesh(@Param("productId") String productId, @Param("meshId") String meshId);
}