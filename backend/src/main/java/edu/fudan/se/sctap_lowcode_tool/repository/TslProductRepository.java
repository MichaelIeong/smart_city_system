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
}