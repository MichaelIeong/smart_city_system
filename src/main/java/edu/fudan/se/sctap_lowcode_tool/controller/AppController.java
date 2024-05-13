package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.dto.ApiResponse;
import edu.fudan.se.sctap_lowcode_tool.model.AppInfo;
import edu.fudan.se.sctap_lowcode_tool.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app")
@Tag(name = "AppController", description = "应用控制器")
public class AppController {

    @Autowired
    private AppService appService;

    @Operation(summary = "上传新的应用信息", description = "客户端提交新构建的应用信息")
    @PostMapping("/upload")
    public ApiResponse<Void> createApp(@RequestBody AppInfo appInfo) {
        try {
            appService.saveApp(appInfo);
            return ApiResponse.success("App saved.");
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    @Operation(summary = "根据appID获取应用信息")
    @PostMapping("/{deviceID}")
    public ApiResponse<AppInfo> getApp(@PathVariable int deviceID) {
        try {
            return ApiResponse.success(appService.getInfo(deviceID));
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }

    @Operation(summary = "应用告知后端要在model studio显示", description = "应用执行按钮")
    @PostMapping("/{deviceID}/highlight")
    public ApiResponse<Void> postAppHighlight(@PathVariable int deviceID) {
        try {
            appService.highlightApp(deviceID);
            return ApiResponse.success("Model Studio will highlight this app.");
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
}
