package edu.fudan.se.sctap_lowcode_tool.controller;

import com.google.gson.Gson;
import edu.fudan.se.sctap_lowcode_tool.utils.AppData;
import edu.fudan.se.sctap_lowcode_tool.business.AppHandle;
import edu.fudan.se.sctap_lowcode_tool.model.AppInfo;
import edu.fudan.se.sctap_lowcode_tool.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 1. 处理用户传回的构造的应用
 * 2. 应该有一个list，里面是已经构造好的应用的名字，点击这个名字，很根据app构造好表单。
 * 3. 可以删除列表中的app
 * 4.
 */
@RestController
@CrossOrigin
@RequestMapping("/api/upload")
@Tag(name = "AppController", description = "应用控制器")
public class AppController {

    @Autowired
    private AppService appService;

    AppHandle appHandle;

    Gson gson = new Gson();

    @Operation(summary = "上传新的应用信息", description = "客户端提交新构建的应用信息")
    @PostMapping("/dsl")
    public ResponseEntity<Void> createApp(@RequestBody AppData appData) {
        //step1：把这个app保存到数据库
        AppInfo appInfo = new AppInfo();
        appInfo.setAppJson(appData.toString());
        appService.saveApp(appInfo);
        //step2：处理trigger，比如需要根据location查找温度传感器，获取室内的温度。处理action，获取action所需的location和device，当温度大于30度，调用device
        appHandle.appExecute(appData);
        //step3：处理modelstudio的高亮显示

        /**
         * 如何使用 SDK
         * step1: 准备工作
         *
         * 通过 TacOS 提供的 Space Stuido、空间管理产品对空间进行初始化。
         *
         * 详细操作手册咨询 TacOS 对应的产品和实施人员
         *
         * step2: 使用 SDK
         *
         * 点击 快速上手 查看 SDK 使用的详细流程。
         */
        //那就是把application传给Space Studio，让它通过sdk来执行？
        //或者不直接传app，比如我要监控空间的温度，把spaceName传给Studio，让他调用，并将数据返回给我
        return ResponseEntity.ok().build();
    }

    /**
     * 暂时不需要，后端仅预留接口
     * @param deviceID
     * @return
     */
    @Operation(summary = "根据appID获取应用信息")
    @PostMapping("/{deviceID}")
    public ResponseEntity<AppInfo> getApp(@PathVariable int deviceID) {
        try {
            return ResponseEntity.ok(appService.getInfo(deviceID));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 暂时不需要，后端仅预留接口
     * @param
     * @return
     */
    @Operation(summary = "获取数据库中所有的application")
    @PostMapping("/")
    public ResponseEntity<List<AppData>> getAppList() {
        try {
            List<AppInfo> appInfos = appService.findAllApplication();
            List<AppData> appDataList = new ArrayList<>();
            for(AppInfo appInfo : appInfos) {
                AppData appData = gson.fromJson(appInfo.getAppJson(),AppData.class);
                appDataList.add(appData);
            }
            return ResponseEntity.ok(appDataList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 根据application的name，删除application,但是表单里没有name字段，
     * 所以说，用户看起来根据name删除，实则根据id删除
     * @param
     * @return
     */
    @Operation(summary = "根据application的name，删除application")
    @DeleteMapping("/")
    public ResponseEntity<Void> deleteApp(@PathVariable String appName) {
        try {
            appService.deleteAppByName(appName);
            return ResponseEntity.ok().build();
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
