package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.EventInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EventService {

    List<EventInfo> getHistory(int deviceID);
}
