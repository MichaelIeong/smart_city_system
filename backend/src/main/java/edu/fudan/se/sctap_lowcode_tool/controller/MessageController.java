package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.model.MessageInfo;
import edu.fudan.se.sctap_lowcode_tool.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @GetMapping("/{uuid}")
    public ResponseEntity<List<MessageInfo>> queryById(
            @PathVariable("uuid") String uuid) {
        return ResponseEntity.ok(messageService.findAllByUuid(uuid));
    }
}
