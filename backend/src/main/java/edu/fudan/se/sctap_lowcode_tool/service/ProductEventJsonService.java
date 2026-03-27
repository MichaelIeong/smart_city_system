package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.model.ProductEventJson;
import edu.fudan.se.sctap_lowcode_tool.repository.ProductEventJsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductEventJsonService {

    @Autowired
    private ProductEventJsonRepository repository;

    /**
     * 根据 productEvent 获取对应的 eventFormat
     * @param productEvent 主键 ID
     * @return JsonNode 格式的 eventFormat
     */
    public JsonNode getEventFormatByProductEvent(String productEvent) {
        return repository.findById(productEvent)
                .map(ProductEventJson::getEventFormat)
                .orElse(null);
    }
}