package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceConfig;
import edu.fudan.se.sctap_lowcode_tool.DTO.DeviceConfigRequest;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceConfiguration;
import edu.fudan.se.sctap_lowcode_tool.service.LHAService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/LHA")
@Tag(name = "LHAController", description = "iot设备的LHA描述")
public class LHAController {
    // 接受前端的生成模型命令，查阅后端数据库，映射成为类，并自动构建LHA模型
    @Autowired
    private LHAService lhaService;

    /**
     * 添加/更新设备描述信息（点击设备描述按钮，弹出文本框，修改填写，点确定）
     */
    @PostMapping("/addConfig")
    public ResponseEntity<Void> addConfig(@RequestBody DeviceConfig deviceConfig) throws JsonProcessingException {
        //有则更新，无则新增
        System.out.println(deviceConfig.getDeviceName());
        DeviceConfiguration deviceConfiguration = lhaService.findConfigurationById(deviceConfig.getDeviceId()).get();
        if(deviceConfiguration != null){
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(deviceConfig);
            deviceConfiguration.setConfiguration(jsonString);
            lhaService.updateConfiguration(deviceConfiguration);
        } else {
            DeviceConfiguration newDeviceConfiguration = new DeviceConfiguration();
            newDeviceConfiguration.setDeviceName(deviceConfig.getDeviceName());
            newDeviceConfiguration.setDeviceId(deviceConfig.getDeviceId());
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(deviceConfig);
            newDeviceConfiguration.setConfiguration(jsonString);
            lhaService.updateConfiguration(deviceConfiguration);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 查看设备描述信息（点击设备描述按钮，弹出文本框）
     */
    @GetMapping("/getConfig")
    public ResponseEntity<?> getConfig(@RequestParam int deviceId) throws JsonProcessingException {
        System.out.println(deviceId);
        DeviceConfiguration deviceConfiguration = lhaService.findConfigurationById(deviceId).get();
        // 创建 ObjectMapper 对象
        ObjectMapper objectMapper = new ObjectMapper();
        // 使用 ObjectMapper 将 JSON 字符串反序列化为 User 对象
        DeviceConfig deviceConfig = objectMapper.readValue(deviceConfiguration.getConfiguration(), DeviceConfig.class);
        return ResponseEntity.ok(deviceConfig);
    }

    /**
     * 生成LHA模型
     */
//    @GetMapping("/generateLHA")
//    public ResponseEntity<?> generateLHA(@RequestParam int deviceId){
//        DeviceConfiguration deviceConfiguration = lhaService.findConfigurationById(deviceId).get();
//        String lha = lhaService.generateLHA(deviceConfiguration.getConfiguration());
//        //写入数据库
//        deviceConfiguration.setLha(lha);
//        lhaService.updateConfiguration(deviceConfiguration);
//        return ResponseEntity.ok(lha);
//    }

    /**
     * 查询设备的LHA模型
     */
    @GetMapping("/getLHA")
    public ResponseEntity<?> getLHA(@RequestParam int deviceId){
        DeviceConfiguration deviceConfiguration = lhaService.findConfigurationById(deviceId).get();
        if(deviceConfiguration.getLha() != null){
            System.out.println(deviceConfiguration.getLha());
            return ResponseEntity.ok(deviceConfiguration.getLha());
        } else{
            String lha = lhaService.generateLHA(deviceConfiguration.getConfiguration());
            //写入数据库
            deviceConfiguration.setLha(lha);
            System.out.println(lha);
            lhaService.updateConfiguration(deviceConfiguration);
            return ResponseEntity.ok(lha);
        }
    }

    /**
     * 修改设备的LHA模型
     */
    @PostMapping("/updateLHA")
    public ResponseEntity<?> updateLHA(@RequestParam int deviceId, @RequestBody JsonNode lha){
        DeviceConfiguration deviceConfiguration = lhaService.findConfigurationById(deviceId).get();
        System.out.println(deviceId);
        deviceConfiguration.setLha(lha.toString());
        lhaService.updateConfiguration(deviceConfiguration);
        return ResponseEntity.ok().build();
    }
}
