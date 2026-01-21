package edu.fudan.se.sctap_lowcode_tool;

import edu.fudan.se.sctap_lowcode_tool.service.event_fusion_2026_jan.engine_component.EventIngestor;
import edu.fudan.se.sctap_lowcode_tool.utils.TslApiUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
class EventIngestorTest {

    @Autowired
    private EventIngestor.TslScheduledFetcher tslScheduledFetcher;

    @Test
    void test() throws TslApiUtil.TslApiException {
        var res = tslScheduledFetcher.fetchEventsSince(
            LocalDate.of(2026, 1, 5).atStartOfDay()
        );
        System.out.println(res.size());
        res.stream()
           .map(e -> {
               String eventId = String.valueOf(e.get("id"));
               String eventType = String.valueOf(e.get("eventType"));
               long createTime = Long.parseLong(String.valueOf(e.get("createTime")));
               LocalDateTime createdAt =
                   LocalDateTime.ofEpochSecond(createTime / 1000, 0, java.time.ZoneOffset.ofHours(8));
               String dutyNetwork = String.valueOf(e.get("dutyNetwork"));
               return "Event{id=" + eventId + ", type=" + eventType + ", createdAt=" + createdAt + ", dutyNetwork=" + dutyNetwork + "}";
           })
            .filter(t -> t.contains("truck_dect"))
           .forEach(System.out::println);

    }

    @Test
    void doFetch() {
        tslScheduledFetcher.setLastFetchTime(LocalDate.of(2026, 1, 5).atStartOfDay());
        tslScheduledFetcher.scheduleFetch();
    }


}