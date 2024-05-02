package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.dto.ApiResponse;
import edu.fudan.se.sctap_lowcode_tool.model.AppInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app")
@Tag(name = "AppController", description = "应用控制器")
public class AppController {

    @Operation(summary = "构造好的应用传给后端", description = "应用发送按钮")
    @PostMapping("/")
    public ApiResponse<Void> postApp(@RequestBody AppInfo appInfo) {
        try {
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    @Operation(summary = "根据appID获取应用")
    @PostMapping("/")
    public ApiResponse<Void> getApp(@RequestParam int appID) {
        try {
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    @Operation(summary = "应用告知后端要在modelstudio显示", description = "应用执行按钮")
    @PostMapping("/highlight")
    public ApiResponse<Void> postAppHighlight(@RequestParam int appID) {
        try {
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
}
