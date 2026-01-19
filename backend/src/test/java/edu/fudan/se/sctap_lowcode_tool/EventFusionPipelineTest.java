package edu.fudan.se.sctap_lowcode_tool;

import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.DataEvent;
import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component.EventIngestor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule.EventSource.sensorEvent;

@SpringBootTest
class EventFusionPipelineTest {

    @Autowired
    private EventIngestor.DirectPushIngestor directPushIngestor;

    @Test
    void testPipelineFlow() {
        int N = 50;

        for (int i = 0; i < N; i++) {
            DataEvent event = DataEvent.builder()
                    .timestamp(System.currentTimeMillis())
                    .sourceIngestor(directPushIngestor.getSourceId())
                    .eventSource(sensorEvent)
                    .identifier(String.valueOf(i))
                    .eventId("vehicle_detector")
                    .payload(Map.of("index", i, "value", "random-data"))
                    .build();

            System.out.println(">>> [Test] DirectPushIngestor pushing event: " + event.getIdentifier());
            directPushIngestor.push(event);
        }

        // 由于 Pipeline 是异步处理的，这里简单 sleep 等待日志输出
        try {
            System.out.println(">>> [Test] Waiting for async processing...");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
