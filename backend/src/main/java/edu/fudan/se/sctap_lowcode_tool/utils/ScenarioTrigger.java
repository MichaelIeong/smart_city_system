package edu.fudan.se.sctap_lowcode_tool.utils;


import java.util.List;

public class ScenarioTrigger {
    private List<List<String>> event_type;
    private List<String> filter;

    public List<List<String>> getEvent_type() {
        return event_type;
    }

    public void setEvent_type(List<List<String>> event_type) {
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

