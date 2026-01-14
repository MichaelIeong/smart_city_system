package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.model.Product;
import edu.fudan.se.sctap_lowcode_tool.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取所有设备类型（用于第一个下拉框）
     */
    public List<Map<String, String>> getDeviceTypes() {
        return productRepository.findAll().stream()
                .map(p -> Map.of("value", p.getProductId(), "label", p.getProductName()))
                .collect(Collectors.toList());
    }

    /**
     * 根据 product_id 获取功能列表（用于第二个下拉框）
     */
    public List<String> getFunctionList(String productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return new ArrayList<>();
        }

        String functionStr = productOpt.get().getProductFunction();
        if (functionStr == null || functionStr.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 尝试解析为 List<String>
            return objectMapper.readValue(functionStr, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // 如果不是 JSON 数组，则视为单个字符串
            return Arrays.asList(functionStr);
        }
    }

    /**
     * 根据 product_id 获取参数 JSON（直接返回 product_json 字段）
     */
    public String getParamJson(String productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent() && productOpt.get().getProductJson() != null) {
            return productOpt.get().getProductJson();
        }
        return "{}";
    }
}