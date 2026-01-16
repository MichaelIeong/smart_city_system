package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.DataSourceType;
import edu.fudan.se.sctap_lowcode_tool.service.DataSourceTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/node-red")
public class DataSourceTypeController {

    @Autowired
    private DataSourceTypeService service;

    @GetMapping("/datasourceType")
    public List<Map<String, Object>> listAll() {
        return service.listAll()
                .stream()
                .map(t -> Map.<String, Object>of(
                        "id", t.getId(),
                        "datasourceType", t.getDatasourceType()
                ))
                .toList();
    }

    // 如果你之后需要新增 DataSourceType，可以启用 POST 功能：
    @PostMapping("/datasourceType")
    public Map<String, Object> addType(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("datasourceType");
        DataSourceType saved = service.create(name);
        return Map.of(
                "id", saved.getId(),
                "datasourceType", saved.getDatasourceType()
        );
    }
}
