package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.Model.DeviceInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "DeviceController", description = "设备状态控制器")
@ApiResponses
public class DeviceController {

    @Operation(summary = "得到设备的状态", description = "是融合感知于sctap执行两个功能需要用到，sctap前端构造应用只需要设置“当温度大于35度则怎么样”，这是不需要设备状态的")
    @GetMapping("/device/status")
    public void getDeviceStatus(@RequestParam("deviceId") String deviceId) {

    }

    @Operation(summary = "得到设备的能力", description = "比如温度传感器测温，扬声器可以出声，这样前端的用户才可以根据这些功能构造应用")
    @GetMapping("/device/ability")
    public void getDeviceAbility(@RequestParam("deviceId") String deviceId) {

    }


    @Operation(summary = "获取设备url", description = "modelstudio将设备信息主要是设备的url，发送给环境表征后端")
    @PostMapping("/device/url")
    public void postDevices(@RequestBody DeviceInfo deviceInfo) {

    }


}
