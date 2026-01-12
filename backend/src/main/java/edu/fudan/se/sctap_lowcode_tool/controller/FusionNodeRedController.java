package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.SensorTypeDTO;
import edu.fudan.se.sctap_lowcode_tool.service.FusionNodeRedService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
