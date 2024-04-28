package edu.fudan.se.sctap_lowcode_tool.result;

import edu.fudan.se.sctap_lowcode_tool.exceptionHandler.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用返回对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResult<T> {
    private long code;
    private String message;
    private T data;

    /**
     * 成功返回结果
     */
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回结果
     */
    public static <T> CommonResult<T> success(T data, String message) {
        return new CommonResult<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败返回结果
     */
    public static <T> CommonResult<T> failed(IErrorCode code) {
        return new CommonResult<>(code.getCode(), code.getMessage(), null);
    }

    /**
     * 失败返回结果
     */
    public static <T> CommonResult<T> failed(String message) {
        return new CommonResult<>(ResultCode.FAILED.getCode(), message, null);
    }

    /**
     * 失败返回结果
     */
    public static <T> CommonResult<T> failed() {
        return failed(ResultCode.FAILED);
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> CommonResult<T> validateFailed(String message) {
        return new CommonResult<>(ResultCode.VALIDATE_FAILED.getCode(), message, null);
    }

    /**
     * 未登录返回结果
     */
    public static <T> CommonResult<T> unauthorized(T data) {
        return new CommonResult<>(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage(), data);
    }

    /**
     * 未授权返回结果
     */
    public static <T> CommonResult<T> forbidden(T data) {
        return new CommonResult<>(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMessage(), data);
    }
}