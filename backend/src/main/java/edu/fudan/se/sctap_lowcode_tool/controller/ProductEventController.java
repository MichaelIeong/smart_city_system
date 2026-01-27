package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.ProductEventJson;
import edu.fudan.se.sctap_lowcode_tool.service.EventService;
import edu.fudan.se.sctap_lowcode_tool.service.ProductEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product_events")
@RequiredArgsConstructor
public class ProductEventController {

    private final ProductEventService eventService;

    // 接口1：获取某个产品下的所有事件详情（包含JSON）
    // GET /api/events?productId=p_ai_camera_tst
    @GetMapping
    public List<Map<String, Object>> getProductEvents(@RequestParam String productId) {
        return eventService.getEventsByProductId(productId);
    }

    // 接口2：直接根据事件ID获取JSON结构
    // GET /api/events/detail/camera_drowning
    @GetMapping("/detail/{eventId}")
    public ProductEventJson getEventJson(@PathVariable String eventId) {
        return eventService.getEventJsonDetail(eventId);
    }
}