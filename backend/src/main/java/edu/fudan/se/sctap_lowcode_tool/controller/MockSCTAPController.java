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

    private static final String MOCK_DATA = """
            {user=234, dsl={Scenario_Trigger={event_type=[], filter=[]}, Scenario_Action=[], Scenario_Description={event_list=[], location=[[]], time_zone=[{type=, start_time=, end_time=}], object_id=[], result=[{result_name=, function_name=, param=}]}}, endTime=, startTime=1726706679224, app=printer add paper}
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
