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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

            // 构建返回的 Map
            Map<String, Object> result = new HashMap<>();
            result.put("id", spaceInfo.getId());
            result.put("projectId", spaceInfo.getProjectInfo().getProjectId());
            result.put("spaceId", spaceInfo.getSpaceId());
            result.put("spaceName", spaceInfo.getSpaceName());
            result.put("description", spaceInfo.getDescription());

            // properties
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

            // 处理 fixedProperties: string -> JSON
            String fixedProperties = spaceInfo.getFixedProperties();
            if (fixedProperties != null && !fixedProperties.trim().isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode fixedPropertiesJson = objectMapper.readTree(fixedProperties);
                    result.put("fixedProperties", fixedPropertiesJson);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.put("fixedProperties", "Invalid JSON format");
                }
            } else {
                result.put("fixedProperties", null);
            }

            // event
            List<Map<String, Object>> eventList = spaceInfo.getEvents().stream()
                    .map(event -> {
                        Map<String, Object> eventMap = new HashMap<>();
                        eventMap.put("eventId", event.getEventId());
                        eventMap.put("eventType", event.getEventType());
                        return eventMap;
                    })
                    .toList();
            result.put("events", eventList);

            // service
            List<Map<String, Object>> serviceList = spaceInfo.getServices().stream()
                    .map(service -> {
                        Map<String, Object> serviceMap = new HashMap<>();
                        serviceMap.put("serviceId", service.getServiceId());
                        serviceMap.put("serviceName", service.getServiceName());
                        return serviceMap;
                    })
                    .toList();
            result.put("services", serviceList);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

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