package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.ProductEvent;
import edu.fudan.se.sctap_lowcode_tool.model.ProductEventJson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ProductEventRepository extends JpaRepository<ProductEvent, String> {

    // 查询某个产品下的所有事件基本信息
    List<ProductEvent> findByProductId(String productId);

    // 🌟 联合查询（自定义查询）：直接返回前端需要的完整结构
    // 获取指定产品的所有事件详情（包含JSON定义）
    @Query(value = """
        SELECT 
            pe.product_id as productId,
            pe.product_event as eventId,
            pe.event_name as eventName,
            pej.event_json as eventJson
        FROM product_event pe
        LEFT JOIN product_event_json pej ON pe.product_event = pej.product_event
        WHERE pe.product_id = :productId
    """, nativeQuery = true)
    List<Map<String, Object>> findFullEventsByProductId(String productId);
}

