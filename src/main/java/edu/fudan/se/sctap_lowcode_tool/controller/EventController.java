package edu.fudan.se.sctap_lowcode_tool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Tag(name = "EventController", description = "事件控制器")
public class EventController {

    @Operation(summary = "Sctap前端发送当前事件给后端", description = "SCTAP以及融合感知模块也使用这个api， 之后应用构造的时候会用到")
    @PostMapping("/event/history/post")
    public void postEvent() {

    }


    @Operation(summary = "得到历史事件",description = "让SCTAP构造应用的时候需要此数据")
    @GetMapping("/event/history/get")
    public void getEvent() {

    }


    @Operation(summary = "构造好的应用传给后端", description = "后端让ms进行高亮显示，就相当于执行了构造的应用")
    @PostMapping("/evnet/highlight")
    public void postEventHighlight() {

    }
}
