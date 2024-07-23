package edu.fudan.se.sctap_lowcode_tool.utils;


import java.util.List;
import lombok.Data;

@Data
public class ScenarioDescription {
    private List<String> eventList;
    private List<List<String>> location;
    private List<TimeZone> timeZone;
    private List<String> objectId;
    private List<Result> result;

    @Data
    public static class TimeZone {
        private String type;
        private String startTime;
        private String endTime;

        public TimeZone() {}

        public TimeZone(String type, String startTime, String endTime) {
            this.type = type;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    @Data
    public static class Result {
        private String resultName;
        private String functionName;
        private String param;

        public Result() {}

        public Result(String resultName, String functionName, String param) {
            this.resultName = resultName;
            this.functionName = functionName;
            this.param = param;
        }
    }
}