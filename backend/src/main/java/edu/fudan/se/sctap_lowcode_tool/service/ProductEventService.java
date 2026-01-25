package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.ProductEventJson;
import edu.fudan.se.sctap_lowcode_tool.repository.ProductEventJsonRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.ProductEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductEventService {

    private final ProductEventRepository productEventRepository;
    private final ProductEventJsonRepository productEventJsonRepository;

    /**
     * 根据产品ID获取所有事件及其详细JSON定义
     */
    public List<Map<String, Object>> getEventsByProductId(String productId) {
        return productEventRepository.findFullEventsByProductId(productId);
    }

    /**
     * 根据事件ID直接获取JSON定义（单个）
     */
    public ProductEventJson getEventJsonDetail(String eventId) {
        return productEventJsonRepository.findById(eventId).orElse(null);
    }
}