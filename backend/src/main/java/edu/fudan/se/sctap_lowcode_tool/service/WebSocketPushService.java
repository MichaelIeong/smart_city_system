package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.AlertMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketPushService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 向前端推送消息
     * */
    public void sendAlert(String eventType, String location, String command) {
        AlertMessage alertMessage = new AlertMessage();
        alertMessage.setEventType(eventType);
        alertMessage.setLocation(location);
        alertMessage.setCommand(command);
        alertMessage.setTime(LocalDateTime.now().toString());
        // 推送到前端订阅的 topic
        simpMessagingTemplate.convertAndSend("/topic/alerts", alertMessage);
        log.info("✅ 推送至前端 -> 事件类型: {}, 位置: {}", eventType, location);
    }
}
