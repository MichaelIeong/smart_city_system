package edu.fudan.se.sctap_lowcode_tool.controller;



import edu.fudan.se.sctap_lowcode_tool.bean.AppData;
import edu.fudan.se.sctap_lowcode_tool.model.AppInfo;
import edu.fudan.se.sctap_lowcode_tool.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/upload")
@Tag(name = "AppController", description = "应用控制器")
public class AppController {

    @Autowired
    private AppService appService;

    @Operation(summary = "上传新的应用信息", description = "客户端提交新构建的应用信息")
    @PostMapping("/dsl")
    public ResponseEntity<Void> createApp(@RequestBody String js) {
        String json = "{ \"user\": \"seer\", \"dsl\": { \"Scenario_Trigger\": { \"event_type\": [ [ \"Noisy_Detected\" ] ], \"filter\": [ \"location is DiningArea05\" ] } }, \"endTime\": \"\", \"startTime\": 1715766137691, \"app\": \"overwork\" }";
        //ObjectMapper objectMapper = new ObjectMapper();

        try{
            //AppData appData = objectMapper.readValue(json,AppData.class);
            System.out.println(8888);
//            System.out.println(appData.getApp());
//            System.out.println(appData.getDsl().getScenario_Trigger());
            //ObjectMapper mapper = new ObjectMapper();
            //String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(appData);
            //System.out.println(jsonString);
        }catch (Exception e){
            e.printStackTrace();
        }

//        try {
//            appService.saveApp(appData);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
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
