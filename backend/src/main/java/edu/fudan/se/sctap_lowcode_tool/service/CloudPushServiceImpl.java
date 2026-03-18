package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.AlertMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
// 当配置为 cloud，或者没有配置这个属性时，此类生效
@ConditionalOnProperty(name = "app.node-role", havingValue = "cloud", matchIfMissing = true)
public class CloudPushServiceImpl implements UnifiedPushService{

    @Resource
    private WebSocketPushService webSocketPushService;

    @Override
    public void pushAlert(AlertMessage alertMessage) {
        // 云端模式：直接通过 WebSocket 推送给前端浏览器
        webSocketPushService.sendAlert(alertMessage);
        log.info("【云端模式】已将消息直接推送到前端 WebSocket: {}", alertMessage.getType());
    }
}
