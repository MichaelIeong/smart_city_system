package edu.fudan.se.sctap_lowcode_tool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "DeviceStatusController", description = "设备状态控制器")
public class DeviceStatusController {


    @Operation(summary = "得到设备的状态，这个应该是融合感知于sctap执行两个功能需要用到的，因为sctap前端构造应用只需要设置“当温度大于35度则怎么样”，这是不需要设备状态的")
    @GetMapping("/device/status")
    public void getDeviceStatus() {

    }

    @Operation(summary = "得到设备的能力，比如温度传感器测温，扬声器可以出声，这样前端的用户才可以根据这些功能构造应用")
    @GetMapping("/device/ability")
    public void getDeviceAbility() {

    }


    @Operation(summary = "Sctap前端制造一些历史事件发给后端，之后应用构造的时候会用到")
    @PostMapping("/event")
    public void postEvent() {

    }


    @Operation(summary = "得到历史事件")
    @GetMapping("/event")
    public void getEvent() {

    }


    @Operation(summary = "构造好的应用，传给后端，然后后端让ms进行高亮显示，就相当于执行了构造的应用。前端是以怎样的形式传过来的呢")
    @PostMapping("/evnet/highlight")
    public void postEventHighlight() {

    }


}
