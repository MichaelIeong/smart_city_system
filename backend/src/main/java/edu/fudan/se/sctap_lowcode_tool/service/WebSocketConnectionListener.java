package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.AlertMessage;
import edu.fudan.se.sctap_lowcode_tool.constant.CommandConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
            Map<String, Map<String, List<String>>> snapshot = new HashMap<>(appRuleExecutorService.getAppRuleLogMap());
            if (!snapshot.isEmpty()) {
                snapshot.forEach((eventType, waitMap) -> waitMap.forEach((waitValue, logs) -> {
                    webSocketPushService.sendAlert(eventType, waitValue, CommandConstant.COMMAND_START);
                }));
                log.info("✅ 已向新连接推送 {} 条正在执行的应用规则", snapshot.size());
            }

            // ==========================
            // ② 推送模拟数据（用于调试）
            // ==========================
            log.info("✅ 正在推送模拟 ill_parking 数据...");
            String eventType = "ill_parking";
            for (int i = 1; i <= 10; i++) {
                String location = String.format("%08d", i);
                webSocketPushService.sendAlert(eventType, location, CommandConstant.COMMAND_START);
            }
            log.info("✅ 模拟数据发送完成，共 10 条");
        });
    }
}
