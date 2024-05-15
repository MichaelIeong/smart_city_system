package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.EventInfo;
import edu.fudan.se.sctap_lowcode_tool.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event")
@Tag(name = "EventController", description = "事件控制器")
public class EventController {

    @Autowired
    private EventService eventService;

    @Operation(summary = "发送当前事件给后端", description = "SCTAP以及融合感知模块也使用这个api，之后应用构造的时候会用到")
    @PostMapping("/upload")
    public ResponseEntity<Void> postEvent(@RequestBody EventInfo eventInfo) {
        try {
            eventService.saveEvent(eventInfo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "得到历史事件", description = "让SCTAP构造应用的时候需要此数据")
    @GetMapping("/{deviceID}/history")
    public ResponseEntity<List<EventInfo>> getHistory(@PathVariable int deviceID) {
        try {
            return ResponseEntity.ok(eventService.getHistory(deviceID));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
