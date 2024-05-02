package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/device")
@Tag(name = "DeviceController", description = "设备状态控制器")
public class DeviceController {

    @Operation(summary = "发送设备的所有信息", description = "modelstudio发送设备信息，主要是设备的url, status和capabilities，发送给环境表征后端")
    @PostMapping("/")
    public ApiResponse<Void> postDevices(@RequestBody DeviceInfo deviceInfo) {
        try {
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    @Operation(summary = "返回设备的状态", description = "设备在线或离线")
    @GetMapping("/status")
    public ApiResponse<Void> getDeviceStatus(@RequestParam("deviceID") int deviceID) {
        try {
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    @Operation(summary = "返回设备的URL", description = "供后端调用设备")
    @GetMapping("/url")
    public ApiResponse<Void> getDeviceURL(@RequestParam("deviceID") int deviceID) {
        try {
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    @Operation(summary = "返回设备的数据", description = "是融合感知于sctap执行两个功能需要用到，sctap前端构造应用只需要设置“当温度大于35度则怎么样”，这是不需要设备状态的")
    @GetMapping("/data")
    public ApiResponse<Void> getDeviceData(@RequestParam("deviceID") int deviceID) {
        try {
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    @Operation(summary = "返回设备的能力", description = "比如温度传感器测温，扬声器可以出声，这样前端的用户才可以根据这些功能构造应用")
    @GetMapping("/capabilities")
    public ApiResponse<Void> getDeviceCapabilities(@RequestParam("deviceID") int deviceID) {
        try {
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }


}
