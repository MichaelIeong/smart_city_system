package edu.fudan.se.sctap_lowcode_tool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DeviceStatusController {

    @GetMapping("/device/status")
    public void getDeviceStatus() {

    }

    @GetMapping("/device/ability")
    public void getDeviceAbility() {

    }

    @PostMapping("/event")
    public void postEvent() {

    }

    @PostMapping("/evnet/highlight")
    public void postEventHighlight() {

    }


}
