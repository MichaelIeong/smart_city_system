package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app")
@Tag(name = "AppController", description = "应用控制器")
public class AppController {

    @Operation(summary = "构造好的应用传给后端", description = "应用发送按钮")
    @PostMapping("/")
    public ApiResponse<Void> postApp(@RequestBody edu.fudan.se.sctap_lowcode_tool.Model.AppInfo appInfo) {
        try {
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.failed(e.getMessage());
        }
    }
}
