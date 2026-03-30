package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.constant.RoleConstant;
import edu.fudan.se.sctap_lowcode_tool.model.EdgeNode;
import edu.fudan.se.sctap_lowcode_tool.model.TslDevice;
import edu.fudan.se.sctap_lowcode_tool.model.TslProduct; // 确保引入了这个
import edu.fudan.se.sctap_lowcode_tool.repository.EdgeNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.TslDeviceRepository;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/api/devices")
public class DeviceBatchController {

    @Autowired
    private TslDeviceRepository tslDeviceRepository;

    @Autowired
    private EdgeNodeRepository edgeNodeRepository;

    @Value("${app.node-role:edge}")
    private String nodeRole;

    @Resource
    private RestTemplate restTemplate;

    /**
     * 内部 DTO 类：专门用来接收前端发来的扁平化 JSON 数据
     * 这里的字段类型必须和前端传过来的完全一致
     */
    @Data
    public static class DeviceImportDto {
        private Integer id;
        private Long projectId;
        private String deviceName;
        private Long deviceId;
        private String productId; // ✅ 接收 String 类型的 ID
        private Integer status;
        private String meshId;
        private String meshNo;
        private String meshName;
        private String meshNature;
        private Double meshArea;
        private String address;
        private String createdAt;
    }

    @PostMapping("/batchAdd")
    public Map<String, Object> batchAddDevices(@RequestBody List<DeviceImportDto> dtoList) {
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("接收到设备同步请求，数量: " + dtoList.size());

            List<TslDevice> entities = new ArrayList<>();

            for (DeviceImportDto dto : dtoList) {
                TslDevice device = new TslDevice();

                // 1. 基础字段直接拷贝
                device.setId(dto.getId()); // 使用 CSV 里的 ID
                device.setProjectId(dto.getProjectId());
                device.setDeviceName(dto.getDeviceName());
                device.setDeviceId(dto.getDeviceId());
                device.setStatus(dto.getStatus());
                device.setMeshId(dto.getMeshId());
                device.setMeshNo(dto.getMeshNo());
                device.setMeshName(dto.getMeshName());
                device.setMeshNature(dto.getMeshNature());
                device.setMeshArea(dto.getMeshArea());
                device.setAddress(dto.getAddress());
                device.setCreatedAt(dto.getCreatedAt());

                // 2. 手动处理 Product 关联
                // 只有当 productId 不为空时才设置关联
                if (dto.getProductId() != null) {
                    TslProduct product = new TslProduct();

                    product.setProductId(dto.getProductId());

                    device.setProduct(product);
                }

                entities.add(device);
            }

            // 3. 批量保存
            tslDeviceRepository.saveAll(entities);

            // 4. 如果是云端节点，负责过滤并下发给各个边缘节点
            if (RoleConstant.CLOUD.equals(nodeRole)) {
                dispatchToEdgeNodes(dtoList);
            }

            result.put("success", true);
            result.put("message", "成功同步 " + entities.size() + " 条设备数据");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "保存失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 将全量设备数据按 gridId 过滤，并下发给对应的边缘节点
     */
    private void dispatchToEdgeNodes(List<DeviceImportDto> allDevices) {
        List<EdgeNode> edgeNodes = edgeNodeRepository.findAll();
        if (edgeNodes.isEmpty()) {
            log.info("未配置边缘节点，跳过设备数据下发。");
            return;
        }

        for (EdgeNode node : edgeNodes) {
            // 使用 Java 8 Stream 过滤出当前边缘节点需要的设备
            List<DeviceImportDto> edgeDevices = allDevices.stream()
                    .filter(dto -> dto.getMeshId() != null && dto.getMeshId().equals(node.getGridId()))
                    .collect(Collectors.toList());
            if (edgeDevices.isEmpty()) {
                continue; // 该边缘节点没有匹配的设备数据，直接跳过
            }
            try {
                // 使用 RestTemplate 发送 POST 请求，由于边端接收的是 @RequestBody List<DeviceImportDto>，直接把 List 传过去即可
                restTemplate.postForEntity(node.getIpAddress() + "/api/devices/batchAdd", edgeDevices, Map.class);
                log.info("边缘节点 [{}] 设备数据同步下发成功", node.getIpAddress());
            } catch (Exception e) {
                // 捕获单个节点的网络异常，防止某一个边端断网导致整个云端的保存逻辑回滚或中断
                log.error("向边缘节点 [{}] 同步设备数据失败: {}", node.getIpAddress(), e.getMessage());
            }
        }
    }
}