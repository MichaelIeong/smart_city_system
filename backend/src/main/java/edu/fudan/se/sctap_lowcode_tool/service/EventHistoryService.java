package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.EventHistory;
import edu.fudan.se.sctap_lowcode_tool.repository.EventHistoryRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class EventHistoryService {
    @Resource
    private EventHistoryRepository eventHistoryRepository;

    @Transactional(transactionManager = "jpaTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void saveEventHistory(EventHistory eventHistory) {
        eventHistoryRepository.save(eventHistory);
    }
}
