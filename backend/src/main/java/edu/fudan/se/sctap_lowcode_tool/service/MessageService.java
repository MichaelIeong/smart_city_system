package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.MessageInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    public List<MessageInfo> findAllByUuid(String uuid) {
        return messageRepository.findMessagesByUuidExcludingSystemMessages(uuid);
    }
}
