package edu.fudan.se.sctap_lowcode_tool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "DeviceStatusController", description = "设备状态控制器")
public class DeviceStatusController {

    @Operation(summary = "得到设备的状态", description = "是融合感知于sctap执行两个功能需要用到，sctap前端构造应用只需要设置“当温度大于35度则怎么样”，这是不需要设备状态的")
    @GetMapping("/device/status")
    public void getDeviceStatus() {

    }

    @Operation(summary = "得到设备的能力", description = "比如温度传感器测温，扬声器可以出声，这样前端的用户才可以根据这些功能构造应用")
    @GetMapping("/device/ability")
    public void getDeviceAbility() {

    }


    @Operation(summary = "获取设备url", description = "modelstudio将设备信息主要是设备的url，发送给环境表征后端")
    @PostMapping("/device/devurl")
    public void postDevices() {

    }


}
