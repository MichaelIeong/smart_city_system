package edu.fudan.se.sctap_lowcode_tool.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<Type> {
    private int code;
    private String message;
    private Type data;

    // 成功响应，不包含数据
    public static <Type> ApiResponse<Type> success() {
        return new ApiResponse<>(Result.SUCCESS.getCode(), Result.SUCCESS.getMessage(), null);
    }

    // 成功响应，包含数据
    public static <Type> ApiResponse<Type> success(Type data) {
        return new ApiResponse<>(Result.SUCCESS.getCode(), Result.SUCCESS.getMessage(), data);
    }

    // 自定义成功响应，包含消息和数据
    public static <Type> ApiResponse<Type> success(String message, Type data) {
        return new ApiResponse<>(Result.SUCCESS.getCode(), message, data);
    }

    // 失败响应，自定义消息
    public static <Type> ApiResponse<Type> failed(String message) {
        return new ApiResponse<>(Result.FAILED.getCode(), message, null);
    }

    // 特定失败响应
    public static <Type> ApiResponse<Type> failed(Result result) {
        return new ApiResponse<>(result.getCode(), result.getMessage(), null);
    }

    // 参数验证失败响应
    public static <Type> ApiResponse<Type> validateFailed(String message) {
        return new ApiResponse<>(Result.VALIDATE_FAILED.getCode(), message, null);
    }

}