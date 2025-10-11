package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.EventBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.EventTypeNode;
import edu.fudan.se.sctap_lowcode_tool.service.EventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "EventController", description = "事件控制器")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping
    public ResponseEntity<Iterable<EventBriefResponse>> getEventsByProjectId(
            @RequestParam(name = "project") int projectId) {
        return ResponseEntity.ok(eventService.findAllByProjectId(projectId));
    }
    // 事件类型
    // 新增/更新事件类型
    // 查找所有事件类型
    // 根据空间id查找事件类型
    // 根据eventid删除事件类型
    @PostMapping("/type")
    public ResponseEntity<EventTypeNode> addOrUpdateEventType(@RequestBody EventTypeNode eventTypeNode) {
        return ResponseEntity.ok(eventService.saveOrUpdate(eventTypeNode));
    }

    @GetMapping("/type")
    public ResponseEntity<List<EventTypeNode>> getAllEventTypes() {
        return ResponseEntity.ok(eventService.getAllEventTypes());
    }

    @GetMapping("/type/space")
    public ResponseEntity<List<EventTypeNode>> getEventTypesBySpaceId(@RequestParam Integer spaceId) {
        return ResponseEntity.ok(eventService.getEventTypesBySpaceId(spaceId));
    }

    @DeleteMapping("/type/{eventTypeId}")
    public ResponseEntity<Void> deleteEventTypeById(@PathVariable String eventTypeId) {
        eventService.deleteByEventTypeId(eventTypeId);
        return ResponseEntity.noContent().build();
    }
    // 事件实例
    // 新增发布/更新事件实例
    // 获取所有事件实例
    // 根据spaceid获取所有事件实例

}
