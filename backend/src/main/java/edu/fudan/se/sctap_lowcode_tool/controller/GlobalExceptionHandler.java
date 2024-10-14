package edu.fudan.se.sctap_lowcode_tool.controller;

import edu.fudan.se.sctap_lowcode_tool.DTO.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<BadRequestException.ErrorResponse> handleBadRequestException(BadRequestException e) {
        return ResponseEntity.badRequest().body(e.get());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }

}
