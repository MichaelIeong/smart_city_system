package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.ErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ValidationExceptionHandler {

    // =======================  @Valid 校验未通过  =============================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        
        // 构建错误详情列表
        List<ErrorResponse.ErrorDetail> errorDetails = new ArrayList<>();
        
        // 处理字段错误
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errorDetails.add(new ErrorResponse.ErrorDetail(
                fieldError.getField(),
                String.valueOf(fieldError.getRejectedValue()),
                fieldError.getDefaultMessage()
            ));
        }
        
        // 处理全局错误
        bindingResult.getGlobalErrors().forEach(error -> {
            errorDetails.add(new ErrorResponse.ErrorDetail(
                error.getObjectName(),
                Arrays.toString(error.getArguments()),
                error.getDefaultMessage()
            ));
        });
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorResponse(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                "请求体格式有误",
                errorDetails
            )
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        List<ErrorResponse.ErrorDetail> errorDetails = new ArrayList<>();
        
        ex.getAllValidationResults().forEach(result -> {
            String paramName = result.getMethodParameter().getParameterName();
            result.getResolvableErrors().forEach(error -> {
                errorDetails.add(new ErrorResponse.ErrorDetail(
                    paramName != null ? paramName : "unknown",
                    Arrays.toString(error.getArguments()),
                    error.getDefaultMessage()
                ));
            });
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorResponse(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                "请求参数格式有误",
                errorDetails
            )
        );
    }

}
