package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.EventBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public List<EventBriefResponse> findAllByProjectId(Integer projectId) {
        return eventRepository.findAllByProjectId(projectId).stream().map(EventBriefResponse::new).toList();
    }

}
