package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.Model.AppInfo;
import edu.fudan.se.sctap_lowcode_tool.Model.EventInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Tag(name = "EventController", description = "事件控制器")
@ApiResponses
public class EventController {

    @Operation(summary = "Sctap前端发送当前事件给后端", description = "SCTAP以及融合感知模块也使用这个api， 之后应用构造的时候会用到")
    @PostMapping("/event/post")
    public void postEvent(@RequestBody EventInfo eventInfo) {

    }


    @Operation(summary = "得到历史事件",description = "让SCTAP构造应用的时候需要此数据")
    @GetMapping("/event/history/get")
    public void getEvent() {

    }


    @Operation(summary = "构造好的应用传给后端", description = "应用发送按钮")
    @PostMapping("/app/post")
    public void postApp(@RequestBody AppInfo appInfo) {

    }

    @Operation(summary = "应用告知后端要在modelstudio显示", description = "应用执行按钮")
    @PostMapping("/evnet/highlight")
    public void postEventHighlight(@RequestBody int appId) {

    }
}
