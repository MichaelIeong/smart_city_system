package edu.fudan.se.sctap_lowcode_tool.controller;


import edu.fudan.se.sctap_lowcode_tool.bean.AppData;
import edu.fudan.se.sctap_lowcode_tool.business.AppHandle;
import edu.fudan.se.sctap_lowcode_tool.model.AppInfo;
import edu.fudan.se.sctap_lowcode_tool.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/app")
@Tag(name = "AppController", description = "应用控制器")
public class AppController {

    @Autowired
    private AppService appService;

    AppHandle appHandle;

    @Operation(summary = "上传新的应用信息", description = "客户端提交新构建的应用信息")
    @PostMapping("/dsl")
    public ResponseEntity<Void> createApp(@RequestBody AppData appData) {
        //step1：把这个app保存到数据库
        AppInfo appInfo = new AppInfo();
        appInfo.setAppJson(appData.toString());
        appService.saveApp(appInfo);
        //step2：处理trigger，比如需要根据location查找温度传感器，获取室内的温度。
        appHandle.appExecute(appData);
        //开始监测trigger
        //step3：处理action，获取action所需的location和device，当温度大于30度，调用device
        //step4：处理model Studio的高亮显示
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "根据appID获取应用信息")
    @PostMapping("/{deviceID}")
    public ResponseEntity<AppInfo> getApp(@PathVariable int deviceID) {
        try {
            return ResponseEntity.ok(appService.getInfo(deviceID));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "应用告知后端要在model studio显示", description = "应用执行按钮")
    @PostMapping("/{deviceID}/highlight")
    public ResponseEntity<Void> postAppHighlight(@PathVariable int deviceID) {
        try {
            appService.highlightApp(deviceID);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
