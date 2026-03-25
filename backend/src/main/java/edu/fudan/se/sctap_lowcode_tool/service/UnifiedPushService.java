package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.AlertMessage;

public interface UnifiedPushService {
    /**
     * 统一的消息推送入口
     * */
    void pushAlert(AlertMessage alertMessage);
}
