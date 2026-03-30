package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.service.ProductEventJsonService;
import edu.fudan.se.sctap_lowcode_tool.service.ProductEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class ProductEventJsonController {

    @Autowired
    private ProductEventJsonService productEventJsonService;

    @GetMapping("/format")
    public ResponseEntity<JsonNode> getEventFormat(@RequestParam String productEvent) {
        JsonNode format = productEventJsonService.getEventFormatByProductEvent(productEvent);

        if (format == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(format);
    }
}