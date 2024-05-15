package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.EventInfo;

import java.util.List;

public interface EventService {

    List<EventInfo> getHistory(int deviceID);

    void saveEvent(EventInfo eventInfo);
}
