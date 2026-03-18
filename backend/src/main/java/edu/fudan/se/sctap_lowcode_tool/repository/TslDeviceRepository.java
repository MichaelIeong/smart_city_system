package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeSummaryDTO;
import edu.fudan.se.sctap_lowcode_tool.model.TslDevice;
import edu.fudan.se.sctap_lowcode_tool.model.TslProduct;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TslDeviceRepository extends JpaRepository<TslDevice, Long> {

    // 1. 全局聚合：根据场景(meshNature)统计
    @Query("SELECT new edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeSummaryDTO(" +
            "d.product.productName, d.product.productFunction, COUNT(d)) " +
            "FROM TslDevice d WHERE d.meshNature = :sceneType " +
            "GROUP BY d.product.productId, d.product.productName, d.product.productFunction " +
            "ORDER BY COUNT(d) DESC")
    List<DeviceTypeSummaryDTO> findGlobalSummaryByScene(@Param("sceneType") String sceneType);

    // 2. 网格聚合：根据网格编号(meshNo)统计
    @Query("SELECT new edu.fudan.se.sctap_lowcode_tool.DTO.DeviceTypeSummaryDTO(" +
            "d.product.productName, d.product.productFunction, COUNT(d)) " +
            "FROM TslDevice d WHERE d.meshNo = :gridId " +
            "GROUP BY d.product.productId, d.product.productName, d.product.productFunction " +
            "ORDER BY COUNT(d) DESC")
    List<DeviceTypeSummaryDTO> findGridSummaryByGridId(@Param("gridId") String gridId);

    // 3. 根据产品ID查询设备实例列表
    // 对应原本的 queryDeviceInstances 功能
    List<TslDevice> findByProductProductId(String productId);

    // 4. 根据 product_id 和 mesh_id 统计数量
    @Query("SELECT COUNT(d) FROM TslDevice d WHERE d.product.productId = :productId AND d.meshId = :meshId")
    long countByProductAndMesh(@Param("productId") String productId, @Param("meshId") String meshId);
    /**
     * API: /api/devices
     * 获取指定项目下的所有设备，并按场景过滤
     */
    List<TslDevice> findByProjectIdAndMeshNature(Long projectId, String meshNature);

    /**
     * API: /api/devices/instances
     * 根据产品ID查询设备实例，并增加场景过滤
     */
    List<TslDevice> findByProductProductIdAndMeshNature(String productId, String meshNature);

    /**
     * API: /api/meshes/all
     * 获取当前场景下的所有网格 (去重)
     */
    @Query("SELECT DISTINCT d.meshNo, d.meshName FROM TslDevice d WHERE d.meshNature = :meshNature")
    List<Object[]> findDistinctMeshesByScene(@Param("meshNature") String meshNature);
    // 参数类型改为 Long
    boolean existsByDeviceId(Long deviceId);

    // 参数类型改为 Long
    @Modifying
    @Transactional("jpaTransactionManager")
    void deleteByDeviceIdIn(List<Long> deviceIds);

    TslDevice findTopByOrderByIdDesc();
    // 根据产品 ID 和 网格 ID 查询设备列表
    List<TslDevice> findByProductProductIdAndMeshId(String productId, String meshId);

    @Modifying
    @Transactional
    @Query("update TslDevice d set d.status = :status where d.deviceId = :id")
    void updateStatusById(@Param("id") Long id, @Param("status") Integer status);
    
    @Modifying
    void deleteByMeshNature(String meshNature);

    /**
     * 查询指定网格下存在的设备类型（去重）
     */
    @Query("""
        SELECT DISTINCT d.product
        FROM TslDevice d
        WHERE d.meshId = :gridId
        """)
    List<TslProduct> findDistinctProductsByMeshId(@Param("gridId") String gridId);
}