package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.service.TslDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TslDataController {

    private final TslDataService tslDataService;

    public TslDataController(TslDataService tslDataService) {
        this.tslDataService = tslDataService;
    }

    @GetMapping("/consume-kafka")
    public String consumeKafkaMessages() {
        tslDataService.startConsuming();
        return "Kafka 消费已启动！";
    }
}