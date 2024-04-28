package edu.fudan.se.sctap_lowcode_tool.result;


import edu.fudan.se.sctap_lowcode_tool.exceptionHandler.IErrorCode;

/**
 * 常用api状态码
 */
public enum ResultCode implements IErrorCode {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(404, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或token已过期"),
    FORBIDDEN(403, "没有相关权限");

    private long code;

    private String message;

    ResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public long getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
