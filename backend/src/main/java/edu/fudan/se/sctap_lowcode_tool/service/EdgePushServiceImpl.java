package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.AlertMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
// 仅当配置明确为 edge 时，此类生效
@ConditionalOnProperty(name = "app.node-role", havingValue = "edge")
public class EdgePushServiceImpl implements UnifiedPushService {

    @Resource
    private RestTemplate restTemplate;

    @Value("${app.cloud-url}")
    private String cloudUrl;

    @Override
    public void pushAlert(AlertMessage alertMessage) {
        // 边端模式：通过 HTTP 转发给云端的 REST 接口
        try {
            restTemplate.postForEntity(cloudUrl + "/api/tapExecutor/receiveMessage", alertMessage, Void.class);
            log.info("【边端模式】已将消息转发至云端接口: {}", alertMessage.getType());
        } catch (Exception e) {
            log.error("【边端模式】消息转发云端失败，请检查网络或配置的云端地址[{}]: {}", cloudUrl, e.getMessage());
        }
    }
}