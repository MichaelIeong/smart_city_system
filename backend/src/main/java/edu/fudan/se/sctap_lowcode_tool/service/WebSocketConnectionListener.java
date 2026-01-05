package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.AlertMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketConnectionListener {
    private final WebSocketPushService webSocketPushService;

    private final AppRuleExecutorService appRuleExecutorService;

    /**
     * 前端建立连接时触发
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("✅ WebSocket 客户端连接: {}", sessionId);

        // ✅ 异步执行，避免阻塞 STOMP 主线程
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000); // 确保前端订阅建立完成
            } catch (InterruptedException ignored) {}

            // ==========================
            // ① 推送真实正在执行的规则（如果有）
            // ==========================
            Map<String, Map<String, List<AlertMessage>>> snapshot = new HashMap<>(appRuleExecutorService.getAppRuleLogPushMap());
            if (snapshot.isEmpty()) {
                return;
            }
            // 收集所有 AlertMessage
            List<AlertMessage> allMessages = snapshot.values().stream()
                    .flatMap(gridMap -> gridMap.values().stream())
                    .flatMap(List::stream)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (allMessages.isEmpty()) {
                return;
            }
            // 按时间戳从早到晚排序
            allMessages.sort(Comparator.comparing(AlertMessage::getTimestamp));
            // 依次推送到前端
            for (AlertMessage message : allMessages) {
                webSocketPushService.sendAlert(message);
            }
            log.info("✅ 已向新连接推送 {} 条历史执行中的应用规则消息", allMessages.size());
        });
    }
}
