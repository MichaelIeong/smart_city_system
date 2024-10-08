package edu.fudan.se.sctap_lowcode_tool.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/taps")
public class MockSCTAPController {

    private static final String MOCK_DATA =
            """
            {
              "id": 0,
              "user": "Ethan",
              "dsl": {
                "Scenario_Trigger": {
                  "event_type": [["Noisy_Detected"], ["Order_Delivered"], ["Person_Entry"]],
                  "filter": [
                    "location is DiningArea04",
                    "timestamp < 20:42:50",
                    "objectId belongsTo trigger_objectId",
                    "event_data.location < 100"
                  ]
                },
                "Scenario_Action": [
                  {
                    "history_condition": "func1 > 222",
                    "current_condition": "contain_DiningArea01.luminance < 333",
                    "action": {
                      "action_name": "AC_Turn_On",
                      "action_location": ["contain DiningArea01"],
                      "action_param": "func1"
                    }
                  }
                ],
                "Scenario_Description": {
                  "event_list": ["Water_Leak", "Coffee_Start_making"],
                  "location": [[], []],
                  "time_zone": [
                    {
                      "type": "time",
                      "start_time": "last 3 min",
                      "end_time": "trigger_timestamp"
                    },
                    {
                      "type": "time",
                      "start_time": "last 4 min",
                      "end_time": "last 1 min"
                    }
                  ],
                  "object_id": [["trigger_objectId", "vip"], ["trigger_objectId"]],
                  "result": [
                    {
                      "function_name": "count",
                      "param": "Water_Leak",
                      "result_name": "func1"
                    }
                  ]
                }
              },
              "endTime": "",
              "startTime": 1726749750382,
              "app": "printer add paper"
            }
            """;

    public static Map<String, Object> buildResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("result", data);
        response.put("message", "success");
        response.put("timestamp", Instant.now().getEpochSecond());
        return response;
    }

    public static Map<String, Object> mockTapList(Map<String, String> parameters, int totalCount) {
        List<Map<String, Object>> result = new ArrayList<>();
        int pageNo = Integer.parseInt(parameters.get("pageNo"));
        int pageSize = Integer.parseInt(parameters.get("pageSize"));
        int totalPage = (int) Math.ceil((double) totalCount / pageSize);
        int key = (pageNo - 1) * pageSize;
        int nextVal = (pageNo >= totalPage ? totalCount % pageSize : pageSize) + 1;

        Random random = new Random();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (int i = 1; i < nextVal; i++) {
            int tmpKey = key + i;
            Map<String, Object> item = new HashMap<>();
            item.put("id", tmpKey);
            item.put("no", "No " + tmpKey);
            item.put("description", "这是一段描述");
            item.put("callNo", random.nextInt(999) + 1);
            item.put("status", random.nextInt(3));  // 0, 1, or 2
            item.put("updatedAt", dateFormat.format(new Date()));
            item.put("editable", false);
            result.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("pageSize", pageSize);
        response.put("pageNo", pageNo);
        response.put("totalCount", totalCount);
        response.put("totalPage", totalPage);
        response.put("data", result);

        return response;
    }


    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam Map<String, String> queryParams
    ) {
        var data = mockTapList(queryParams, 20);
        var response = buildResponse(data);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> requestBody
    ) {
        log.info("Create request: {}", requestBody);
        return new ResponseEntity<>(buildResponse("Create OK"), HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteByIds(@RequestParam("id") List<String> ids) {
        log.info("Delete request: {}", ids);
        return new ResponseEntity<>(buildResponse("Delete OK"), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateById(
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> requestBody
    ) {
        log.info("Update request: {} - {}", id, requestBody);
        return new ResponseEntity<>(buildResponse("Update OK"), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteById(@PathVariable("id") String id) {
        log.info("Delete request: {}", id);
        return new ResponseEntity<>(buildResponse("Delete OK"), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable("id") String id) {
        log.info("Get request: {}", id);
        return new ResponseEntity<>(buildResponse(MOCK_DATA), HttpStatus.OK);
    }


}
