package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.EventBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.neo4jModel.EventTypeNode;
import edu.fudan.se.sctap_lowcode_tool.neo4jRepository.EventTypeNodeRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventTypeNodeRepository eventTypeNodeRepository;

    public List<EventBriefResponse> findAllByProjectId(Integer projectId) {
        return eventRepository.findAllByProjectId(projectId).stream().map(EventBriefResponse::new).toList();
    }

    public EventTypeNode saveOrUpdate(EventTypeNode eventTypeNode) {
        return eventTypeNodeRepository.save(eventTypeNode);
    }

    public List<EventTypeNode> getAllEventTypes() {
        return eventTypeNodeRepository.findAllEventTypes();
    }

    public List<EventTypeNode> getEventTypesBySpaceId(Integer spaceId) {
        return eventTypeNodeRepository.findBySpaceId(spaceId);
    }

    public void deleteByEventTypeId(String eventTypeId) {
        eventTypeNodeRepository.deleteByEventTypeId(eventTypeId);
    }
}
