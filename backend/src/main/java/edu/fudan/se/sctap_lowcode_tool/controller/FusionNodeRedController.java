package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.DTO.SensorTypeDTO;
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
    @GetMapping("/sensorTypes")
    public List<SensorTypeDTO> listSensorTypes() {
        return fusionService.listSensorTypes();
    }

    /* =====================================================
     * Space Event API
     * ===================================================== */
    @GetMapping("/spaceEventTypes")
    public List<String> listSpaceEventTypes() {
        return fusionService.listSpaceEventTypes();
    }

    /* =====================================================
     * Node-RED Upload Rule API
     * ===================================================== */

    @PostMapping("/uploadRule")
    public Map<String, Object> uploadRule(@RequestBody JsonNode flowJson) {

        String received = fusionService.handleUploadRule(flowJson);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "接收成功");
        response.put("receivedData", received);

        return response;
    }
}
