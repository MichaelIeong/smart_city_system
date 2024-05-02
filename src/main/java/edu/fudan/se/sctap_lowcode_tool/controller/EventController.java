package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.EventInfo;
import edu.fudan.se.sctap_lowcode_tool.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/event")
@Tag(name = "EventController", description = "事件控制器")
public class EventController {

    @Operation(summary = "发送当前事件给后端", description = "SCTAP以及融合感知模块也使用这个api，之后应用构造的时候会用到")
    @PostMapping("/")
    public ApiResponse<Void> postEvent(@RequestBody EventInfo eventInfo) {
        try {
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    @Operation(summary = "得到历史事件", description = "让SCTAP构造应用的时候需要此数据")
    @GetMapping("/history")
    public ApiResponse<List<EventInfo>> getHistory(@RequestParam("deviceID") int deviceID) {
        try {
            List<EventInfo> events = new ArrayList<>();
            return ApiResponse.success(events);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

}