package edu.fudan.se.sctap_lowcode_tool.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.model.PropertyInfo;
import edu.fudan.se.sctap_lowcode_tool.model.SpaceInfo;
import edu.fudan.se.sctap_lowcode_tool.service.SpaceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/spaces")
@Tag(name = "SpaceController", description = "与空间操作相关的API接口")
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSpaceInfoById(@PathVariable int id) {
        Optional<SpaceInfo> spaceInfoOptional = spaceService.findSpaceById(id);

        if (spaceInfoOptional.isPresent()) {
            SpaceInfo spaceInfo = spaceInfoOptional.get();

            Map<String, Object> result = new HashMap<>();
            result.put("id", spaceInfo.getSpaceId());
            result.put("projectId", spaceInfo.getProjectInfo().getProjectId());
            result.put("spaceId", spaceInfo.getSpaceId());
            result.put("spaceName", spaceInfo.getSpaceName());
            result.put("description", spaceInfo.getDescription());

            List<Map<String, Object>> propertiesList = spaceInfo.getProperties().stream()
                    .map(property -> {
                        Map<String, Object> propertyMap = new HashMap<>();
                        PropertyInfo propertyInfo = property.getProperty();
                        if (propertyInfo != null) {
                            propertyMap.put("propertyKey", propertyInfo.getPropertyKey());
                        }
                        propertyMap.put("propertyValue", property.getPropertyValue());
                        return propertyMap;
                    })
                    .collect(Collectors.toList());
            result.put("properties", propertiesList);

            List<Map<String, Object>> eventList = spaceInfo.getEvents().stream()
                    .map(event -> {
                        Map<String, Object> eventMap = new HashMap<>();
                        eventMap.put("eventId", event.getEventId());
                        eventMap.put("eventType", event.getEventType());
                        return eventMap;
                    }).toList();
            result.put("events", eventList);

//            List<Map<String, Object>> serviceList = spaceInfo.getServices().stream()
//                    .map(service -> {
//                        Map<String, Object> serviceMap = new HashMap<>();
//                        serviceMap.put("serviceId", service.getServiceId());
//                        serviceMap.put("serviceName", service.getServiceName());
//                        return serviceMap;
//                    }).toList();
//            result.put("services", serviceList);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getSpaceInfoByProjectId(
            @RequestParam("project") int projectId) {
        List<SpaceInfo> spaces = spaceService.findSpacesByProjectId(projectId);
        if (spaces.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> result = spaces.stream()
                .map(space -> {
                    Map<String, Object> spaceMap = new HashMap<>();
                    spaceMap.put("id", space.getSpaceId());
                    spaceMap.put("spaceId", space.getSpaceId());
                    spaceMap.put("spaceName", space.getSpaceName());
                    return spaceMap;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<SpaceInfo> createSpace(@RequestBody SpaceInfo spaceInfo) {
        SpaceInfo created = spaceService.saveSpace(spaceInfo);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpaceInfo> updateSpace(@PathVariable int id, @RequestBody SpaceInfo spaceInfo) {
        return spaceService.updateSpace(id, spaceInfo)
                .map(updated -> new ResponseEntity<>(updated, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpace(@PathVariable int id) {
        spaceService.deleteSpace(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 获取所有空间的基本信息（spaceId, spaceName）
     */
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getAllSpaces() {
        List<SpaceInfo> spaces = spaceService.findAllSpaces();

        if (spaces.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> result = spaces.stream()
                .map(space -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", space.getSpaceId());
                    map.put("spaceId", space.getSpaceId());
                    map.put("spaceName", space.getSpaceName());
                    return map;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}