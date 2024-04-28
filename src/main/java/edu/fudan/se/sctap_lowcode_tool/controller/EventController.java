package edu.fudan.se.sctap_lowcode_tool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Tag(name = "EventController", description = "事件控制器")
public class EventController {

    @Operation(summary = "Sctap前端发送当前事件给后端，之后应用构造的时候会用到")
    @PostMapping("/event/post")
    public void postEvent() {

    }


    @Operation(summary = "得到历史事件")
    @GetMapping("/event/history")
    public void getEvent() {

    }


    @Operation(summary = "构造好的应用，传给后端，然后后端让ms进行高亮显示，就相当于执行了构造的应用。前端是以怎样的形式传过来的呢")
    @PostMapping("/evnet/highlight")
    public void postEventHighlight() {

    }
}
