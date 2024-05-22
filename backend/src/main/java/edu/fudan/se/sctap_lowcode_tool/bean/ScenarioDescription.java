package edu.fudan.se.sctap_lowcode_tool.bean;

/**
 * @author ：sunlinyue
 * @date ：Created in 2024/5/15 21:36
 * @description：
 * @modified By：
 * @version: $
 */
import java.util.List;

public class ScenarioDescription {
    private List<String> eventList;
    private List<List<String>> location;
    private List<TimeZone> timeZone;
    private List<String> objectId;
    private List<Result> result;

    public ScenarioDescription() {}

    public ScenarioDescription(List<String> eventList, List<List<String>> location, List<TimeZone> timeZone, List<String> objectId, List<Result> result) {
        this.eventList = eventList;
        this.location = location;
        this.timeZone = timeZone;
        this.objectId = objectId;
        this.result = result;
    }

    public List<String> getEventList() {
        return eventList;
    }

    public void setEventList(List<String> eventList) {
        this.eventList = eventList;
    }

    public List<List<String>> getLocation() {
        return location;
    }

    public void setLocation(List<List<String>> location) {
        this.location = location;
    }

    public List<TimeZone> getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(List<TimeZone> timeZone) {
        this.timeZone = timeZone;
    }

    public List<String> getObjectId() {
        return objectId;
    }

    public void setObjectId(List<String> objectId) {
        this.objectId = objectId;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }

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

        public String getResultName() {
            return resultName;
        }

        public void setResultName(String resultName) {
            this.resultName = resultName;
        }

        public String getFunctionName() {
            return functionName;
        }

        public void setFunctionName(String functionName) {
            this.functionName = functionName;
        }

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }
    }
}

