package edu.fudan.se.sctap_lowcode_tool.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    // 成功响应，不包含数据
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(Result.SUCCESS.getCode(), Result.SUCCESS.getMessage(), null);
    }

    // 成功响应，包含数据
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(Result.SUCCESS.getCode(), Result.SUCCESS.getMessage(), data);
    }

    // 成功响应，包含消息
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(Result.SUCCESS.getCode(), message, null);
    }

    // 自定义成功响应，包含消息和数据
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(Result.SUCCESS.getCode(), message, data);
    }

    // 失败响应，自定义消息
    public static <T> ApiResponse<T> failed(String message) {
        return new ApiResponse<>(Result.FAILED.getCode(), message, null);
    }

    // 特定失败响应
    public static <T> ApiResponse<T> failed(Result result) {
        return new ApiResponse<>(result.getCode(), result.getMessage(), null);
    }

    // 参数验证失败响应
    public static <T> ApiResponse<T> validateFailed(String message) {
        return new ApiResponse<>(Result.VALIDATE_FAILED.getCode(), message, null);
    }

}