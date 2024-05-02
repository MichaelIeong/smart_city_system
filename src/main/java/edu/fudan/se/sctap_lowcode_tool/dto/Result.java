package edu.fudan.se.sctap_lowcode_tool.dto;

/**
 * API响应的结果枚举，包括一些常用的状态码和消息。
 */
public enum Result {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(404, "参数检验失败");

    private final int code;
    private final String message;

    Result(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}