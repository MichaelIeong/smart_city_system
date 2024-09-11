package edu.fudan.se.sctap_lowcode_tool.service.impl;

import edu.fudan.se.sctap_lowcode_tool.model.EventInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.EventRepository;
import edu.fudan.se.sctap_lowcode_tool.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventServiceImpl  implements EventService {
    @Autowired
    private EventRepository eventRepository;

    @Override
    public List<EventInfo> getHistory(String deviceID) {
        return List.of();
    }

    @Override
    public void saveEvent(EventInfo eventInfo) {

    }
}
