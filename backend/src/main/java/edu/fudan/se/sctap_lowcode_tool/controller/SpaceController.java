package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.service.SpaceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/spaces")
@Tag(name = "SpaceController", description = "与空间操作相关的API接口")
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getSpaceInfoByProjectId(
            @RequestParam("project") int projectId
    ) {
        List<SpaceInfo> spaces = spaceService.findSpacesByProjectId(projectId);
        if (spaces.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 将 SpaceInfo 转换为 Map<String, Object>
        List<Map<String, Object>> result = spaces.stream()
                .map(space -> {
                    Map<String, Object> spaceMap = new HashMap<>();
                    spaceMap.put("id", space.getId());
                    spaceMap.put("spaceId", space.getSpaceId());
                    spaceMap.put("spaceName", space.getSpaceName());
                    return spaceMap;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}