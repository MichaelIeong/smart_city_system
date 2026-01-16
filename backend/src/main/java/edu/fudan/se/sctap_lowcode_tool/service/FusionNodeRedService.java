package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.SensorTypeDTO;
import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import edu.fudan.se.sctap_lowcode_tool.model.TslProduct;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvEventRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.TslProductRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FusionNodeRedService {

    private final TslProductRepository productRepository;
    private final EnvEventRepository envEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FusionNodeRedService(
            TslProductRepository productRepository,
            EnvEventRepository envEventRepository
    ) {
        this.productRepository = productRepository;
        this.envEventRepository = envEventRepository;
    }

    /* =====================================================
     * Sensor Event（设备事件）
     * ===================================================== */

    /**
     * Node-RED 事件融合：
     * 查询所有 Sensor Types
     */
    public List<SensorTypeDTO> listSensorTypes() {

        List<TslProduct> products = productRepository.findAll();

        return products.stream()
                .map(this::toSensorTypeDTO)
                .collect(Collectors.toList());
    }

    private SensorTypeDTO toSensorTypeDTO(TslProduct product) {
        List<String> sensingEvents = parseEvents(product.getProductEvent());

        return new SensorTypeDTO(
                product.getProductId(),
                product.getProductName(),
                sensingEvents
        );
    }

    /**
     * 将 product_event JSON 字符串解析成 List<String>
     */
    private List<String> parseEvents(String productEvent) {
        if (productEvent == null || productEvent.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(
                    productEvent,
                    new TypeReference<List<String>>() {}
            );
        } catch (Exception e) {
            // ⚠️ 解析失败不影响主流程
            return Collections.emptyList();
        }
    }

    /* =====================================================
     * Space Event（环境 / 跨域事件）
     * ===================================================== */

    public List<String> listSpaceEventTypes() {

        List<EnvEvent> events = envEventRepository.findAll();

        return events.stream()
                .map(EnvEvent::getEventType)
                .filter(type -> type != null && !type.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    /* =====================================================
     * Node-RED Rule Upload（占位）
     * ===================================================== */

    /**
     * 处理 Node-RED 上传的 flow JSON
     * ⚠️ 当前为占位实现，后续在这里做：
     *   - 结构校验
     *   - DSL 转换
     *   - 持久化
     */
    public String handleUploadRule(JsonNode flowJson) {
        // 当前阶段不做任何业务处理
        // 只返回字符串，方便 Controller 直接返回给前端
        return flowJson.toPrettyString();
    }
}
