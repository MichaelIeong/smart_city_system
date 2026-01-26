package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.TslProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface TslProductRepository extends JpaRepository<TslProduct, String> {

    // API: /api/deviceTypes/fromTslProduct
    // 查询在指定场景下有设备实例的产品类型
    @Query("SELECT p FROM TslProduct p WHERE p.productId IN " +
            "(SELECT DISTINCT d.product.productId FROM TslDevice d WHERE d.meshNature = :meshNature)")
    List<TslProduct> findProductsByMeshNature(@Param("meshNature") String meshNature);
    // 根据 projectId 查询设备类型
    List<TslProduct> findByProjectId(Integer projectId);
    /**
     * 并集查询
     * 1. p.project_id = ?1  -> 查当前场景定义的新类型
     * 2. p.project_id = 0/NULL -> 查公共类型
     * 3. d.mesh_nature = ?2 -> 查 tsl_devices 表中该场景正在使用的旧类型
     */
    @Query(value = "SELECT DISTINCT p.* FROM tsl_product p " +
            "LEFT JOIN tsl_devices d ON p.product_id = d.product_id " +
            "WHERE (p.project_id = ?1) " +
            "OR (d.mesh_nature = ?2)",
            nativeQuery = true)
    List<TslProduct> findBySceneDefinitionOrUsage(Integer projectId, String meshNature);
}