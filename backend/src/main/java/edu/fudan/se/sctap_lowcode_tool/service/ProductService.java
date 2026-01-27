package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.model.Product;
import edu.fudan.se.sctap_lowcode_tool.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    //根据gridId获取对应的设备类型
    public List<Map<String, String>> getDeviceTypesByGridId(String gridId) {
        List<Map<String, String>> result = new ArrayList<>();

        try {
            // Step 1: 根据 gridId (mesh_id) 从 tsl_devices 表查询 product_id
            // 注意：使用 DISTINCT 去重
            String sql1 = "SELECT DISTINCT product_id FROM tsl_devices WHERE mesh_id = ?";
            List<String> productIds = jdbcTemplate.queryForList(sql1, String.class, gridId);

            // 如果没有查到数据，直接返回空列表
            if (productIds.isEmpty()) {
                return result;
            }

            // Step 2: 根据 product_id 列表从 tsl_product 表查询 name 等信息
            // 构建 IN 查询的占位符 (?, ?, ?...)
            String inClause = String.join(",", Collections.nCopies(productIds.size(), "?"));
            String sql2 = "SELECT product_id, product_name FROM tsl_product WHERE product_id IN (" + inClause + ")";

            // 执行查询
            List<Map<String, Object>> productDetails = jdbcTemplate.queryForList(sql2, productIds.toArray());

            // Step 3: 组装数据。前端下拉框通常需要 value 和 label
            for (Map<String, Object> row : productDetails) {
                Map<String, String> option = new HashMap<>();
                // value 通常存 product_id (用于后台逻辑)
                option.put("value", row.get("product_id").toString());
                // label 通常存 product_name (用于前端显示)
                option.put("label", row.get("product_name").toString());
                result.add(option);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 建议记录日志 logger.error("查询设备类型失败", e);
        }

        return result;
    }


}