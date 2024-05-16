package edu.fudan.se.sctap_lowcode_tool.bean;

/**
 * @author ：sunlinyue
 * @date ：Created in 2024/5/15 21:35
 * @description：
 * @modified By：
 * @version: $
 */
import java.util.List;

public class ScenarioTrigger {
    private List<List<String>> event_type;
    private List<String> filter;

    public ScenarioTrigger() {}

    public ScenarioTrigger(List<List<String>> event_type, List<String> filter) {
        this.event_type = event_type;
        this.filter = filter;
    }

    public List<List<String>> getEventType() {
        return event_type;
    }

    public void setEventType(List<List<String>> event_type) {
        this.event_type = event_type;
    }

    public List<String> getFilter() {
        return filter;
    }

    public void setFilter(List<String> filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "ScenarioTrigger{" +
                "eventType=" + event_type +
                ", filter=" + filter +
                '}';
    }
}

