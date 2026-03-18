package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.se.sctap_lowcode_tool.DTO.AlertMessage;
import edu.fudan.se.sctap_lowcode_tool.constant.RedisConstant;
import edu.fudan.se.sctap_lowcode_tool.utils.redis.RedisUtil;
import jakarta.annotation.Resource;
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

    @Resource
    private final WebSocketPushService webSocketPushService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ObjectMapper objectMapper;

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

            // 1. 获取推送消息的 redis key
            Set<String> pushKeys = redisUtil.getKeys(RedisConstant.PUSH_LIST_PREFIX);
            if (pushKeys == null || pushKeys.isEmpty()) {
                return;
            }

            // 2. 遍历每个 Key，获取 List 里的所有 JSON 字符串并反序列化
            List<AlertMessage> allMessages = new ArrayList<>();
            for (String key : pushKeys) {
                List<String> msgListStr = redisUtil.getListAll(key);
                for (String msgStr : msgListStr) {
                    try {
                        AlertMessage message = objectMapper.readValue(msgStr, AlertMessage.class);
                        allMessages.add(message);
                    } catch (JsonProcessingException e) {
                        log.error("WebSocket 初始化推送反序列化失败, 数据: {}, 错误: {}", msgStr, e.getMessage());
                    }
                }
            }
            if (allMessages.isEmpty()) {
                return;
            }

            // 3. 按时间戳从早到晚排序
            allMessages.sort(Comparator.comparing(AlertMessage::getTimestamp));

            // 4. 依次推送到前端
            for (AlertMessage message : allMessages) {
                webSocketPushService.sendAlert(message);
            }
            log.info("✅ 已向新连接推送 {} 条历史执行中的应用规则消息", allMessages.size());
        });
    }
}
