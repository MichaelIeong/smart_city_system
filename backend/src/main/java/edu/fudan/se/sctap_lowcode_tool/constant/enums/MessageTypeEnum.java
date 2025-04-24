package edu.fudan.se.sctap_lowcode_tool.constant.enums;

import lombok.Getter;

@Getter
public enum MessageTypeEnum {
    USER_TYPE("user", 0),  // 用户消息
    ASSISTANT_TYPE("assistant", 1),  // AI消息
    SYSTEM_TYPE("system", 2),  // 系统消息
    TOOL_TYPE("tool", 3);  // 工具消息

    private final String type;

    private final int code;

    MessageTypeEnum(String type, int code) {
        this.type = type;
        this.code = code;
    }

    public static MessageTypeEnum getByCode(int code) {
        for (MessageTypeEnum type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

    public static MessageTypeEnum getByType(String type) {
        for (MessageTypeEnum typeEnum : values()) {
            if (typeEnum.getType().equals(type)) {
                return typeEnum;
            }
        }
        return null;
    }
}
