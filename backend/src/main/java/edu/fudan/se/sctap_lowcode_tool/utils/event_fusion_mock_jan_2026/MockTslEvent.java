package edu.fudan.se.sctap_lowcode_tool.utils.event_fusion_mock_jan_2026;

import cn.hutool.core.bean.BeanUtil;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.EventFusionRule;
import edu.fudan.se.sctap_lowcode_tool.DTO.event_fusion_2026_jan.event.DataEvent;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Data
public class MockTslEvent {
    private final String id;
    private final String eventNo = "CA20260121166247";
    private final String eventName = "渣土车识别-卡口-永达路上段01-渣土车抓拍";
    private final String eventTempName = "渣土识别自动流程";
    private final int eventMain = 0;
    private final String eventType = "truck_dect";
    private final String eventTypeName = "渣土车识别";
    private final String eventLevel = "level3";
    private final String fromAlarm = "AL20260121166170";
    private final String dataSource = null;
    private final long eventTime;
    private final int status = 1;
    private final int handleStatus = 0;
    private final int filterStatus = 4;
    private final String filterRef = null;
    private final Words words;
    private final long filterTime;
    private final String handler = "赵永婷";
    private final String handleTime = "剩余9天22时";
    private final String handleFlag = "正常";
    private final String validTime = "ten_day";
    private final String finishTime = null;
    private final String dutyNetwork;
    private final String dutyNetworkName;
    private final String address = "云南省临沧市永德县德党镇永达路永德客运站";
    private final double longitude = 99.25813200;
    private final double latitude = 24.01461800;
    private final String remarks = null;
    private final List<String> photos;
    private final List<String> deviceRef = List.of("2024082100016");
    private final String createUser = "9ae15152afc7458bbefbb2a4ef4b8a4f";
    private final String createUserName = "system";
    private final String lastDoneUserName = null;
    private final String todoUserName = "赵永婷";
    private final long createTime;
    private final String lastUpdateUser = "9ae15152afc7458bbefbb2a4ef4b8a4f";
    private final String lastUpdateUserName = "system";
    private final long lastUpdateTime;
    private final String stopReason = null;
    private final String discardReason = null;
    private final String flowResult = null;
    private final String areaCodes = null;
    private final String areaNames = null;
    private final String reportUserInfo = null;
    private final String dictMap = null;

    public MockTslEvent(
        LocalDateTime eventTime, String plateNumber, Grid dutyNetwork, String photoUrl
    ) {
        long epochMilli = eventTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.eventTime = epochMilli;
        this.words = new Words(plateNumber);
        this.filterTime = epochMilli;
        this.dutyNetwork = dutyNetwork.hash;
        this.dutyNetworkName = dutyNetwork.name;
        this.photos = List.of(photoUrl);
        this.createTime = epochMilli;
        this.lastUpdateTime = epochMilli;
    }

    public MockTslEvent(
        LocalDateTime eventTime, Grid dutyNetwork, Photo photo
    ) {
        this(eventTime, photo.plateNumber, dutyNetwork, photo.url);
    }

    public DataEvent toDataEvent() {
        return DataEvent.builder()
                        .timestamp(createTime)
                        .sourceIngestor("MOCK_TSL_EVENT")
                        .eventSource(EventFusionRule.EventSource.sensorEvent)
                        .identifier("tsl-mock-" + id)
                        .eventId(eventType)
                        .payload(BeanUtil.beanToMap(this))
                        .build();
    }

    @Data
    private static class Words {
        private final String identifier = "truck_detect";
        private final String area_no = "56";
        private final String camera_no = "cb11de38f3a74ce48c1acfb37427c9a4";
        private final String vehicle_type = "";
        private final String target_rois = "[\"853,311\",\"1344,311\",\"1344,979\",\"853,979\"]";
        private final String plate_number;
    }

    public enum Grid {
        GRID_2("848990d972bc4a7ab62321383e669bf3", "永德城区02网格"),
        GRID_3("25d1aa241a3340d5865df6d7fc4d2eea", "永德城区03网格"),
        GRID_4("03e651cb1599490a9b86086f1b78e5ec", "永德城区04网格");
        public final String hash;
        public final String name;
        Grid(String hash, String name) {
            this.hash = hash;
            this.name = name;
        }
        @Override public String toString() {return name;}
    }

    public enum Photo {
        TRICYCLE("https://s3.smartyd.com/parking/662fcfb0-e945-11f0-b6a2-5f58d4e584dc", false, ""),
        TRUCK_3("https://s3.smartyd.com/parking/6ce64ed0-f1db-11f0-b6a2-5f58d4e584dc", true, "云S22468"),
        TRUCK_4("https://s3.smartyd.com/metrics/event/20260109/cbff5b16-5751-418d-a5a3-a81cf6ed1ccd.png", true, ""),
        TRUCK_5("https://s3.smartyd.com/metrics/event/20260109/62270c00-63c7-4237-b826-76108467b347.png", true, ""),
        TRUCK_6("https://s3.smartyd.com/metrics/event/20260109/f6b2a4f6-5374-44e9-9a33-bfb2d27073f9.png", true, "云S82745");
        public final String url;
        public final boolean isSpilling;
        public final String plateNumber;
        Photo(String url, boolean isSpilling, String plateNumber) {
            this.url = url;
            this.isSpilling = isSpilling;
            this.plateNumber = plateNumber;
        }
        @Override public String toString() {
            return name() + (isSpilling ? "-掉渣" : "-未掉渣")
                   + (StringUtils.hasText(plateNumber) ? " (" + plateNumber + ")" : "");
        }
    }
}
