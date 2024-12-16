package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.utils.kafka.TslDataUtil;
import org.springframework.stereotype.Service;

@Service
public class TslDataService {

    private final TslDataUtil tslDataUtil;

    public TslDataService(TslDataUtil tslDataUtil) {
        this.tslDataUtil = tslDataUtil;
    }

    public void startConsuming() {
        System.out.println("启动 Kafka 消息消费...");
        tslDataUtil.consumeMessages();
    }
}