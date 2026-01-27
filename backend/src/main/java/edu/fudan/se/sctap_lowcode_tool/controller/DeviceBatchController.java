package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.TslDevice;
import edu.fudan.se.sctap_lowcode_tool.model.TslProduct; // 确保引入了这个
import edu.fudan.se.sctap_lowcode_tool.repository.TslDeviceRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/devices")
public class DeviceBatchController {

    @Autowired
    private TslDeviceRepository tslDeviceRepository;

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

            result.put("success", true);
            result.put("message", "成功同步 " + entities.size() + " 条设备数据");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "保存失败: " + e.getMessage());
        }
        return result;
    }
}