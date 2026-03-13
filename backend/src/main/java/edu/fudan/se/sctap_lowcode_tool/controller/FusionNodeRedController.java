package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.DTO.SensorTypeDTO;
import edu.fudan.se.sctap_lowcode_tool.DTO.ProductEventDTO;
import edu.fudan.se.sctap_lowcode_tool.service.FusionNodeRedService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/node-red/fusion")
@CrossOrigin
public class FusionNodeRedController {

    private final FusionNodeRedService fusionService;

    public FusionNodeRedController(FusionNodeRedService fusionService) {
        this.fusionService = fusionService;
    }

    /* =====================================================
     * Sensor Event API
     * ===================================================== */
    @GetMapping("/sensorTypesInGrid")
    public List<SensorTypeDTO> listSensorTypesInGrid(@RequestParam String gridId) {
        return fusionService.listSensorTypesInGrid(gridId);
    }

    /* =====================================================
     * Space Event API
     * ===================================================== */
    @GetMapping("/spaceEventTypes")
    public List<String> listSpaceEventTypes() {
        return fusionService.listSpaceEventTypes();
    }

    /* =====================================================
    * Product Event API
    * ===================================================== */

    @GetMapping("/allProductEvents")
    public List<ProductEventDTO> listProductEvents() {
        return fusionService.listProductEvents();
    }

    /* =====================================================
     * Node-RED Upload Rule API
     * ===================================================== */
    @PostMapping("/uploadRule")
    public Map<String, Object> uploadRule(@RequestBody JsonNode flowJson) {

        Map<String, Object> response = new HashMap<>();

        try {
            fusionService.handleUploadRule(flowJson);

            response.put("success", true);
            response.put("message", "规则上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "规则上传失败：" + e.getMessage());
        }

        return response;
    }
}
