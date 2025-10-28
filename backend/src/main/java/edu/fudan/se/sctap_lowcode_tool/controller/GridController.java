package edu.fudan.se.sctap_lowcode_tool.controller;
import edu.fudan.se.sctap_lowcode_tool.service.GridService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

@RestController
@RequestMapping("/api/grid")
@CrossOrigin
public class GridController {

    @Autowired
    private GridService gridService;

    @GetMapping("/{meshId}")
    public Map<String, Object> getGridInfo(@PathVariable String meshId) {
        return gridService.getGridDetail(meshId);
    }
}