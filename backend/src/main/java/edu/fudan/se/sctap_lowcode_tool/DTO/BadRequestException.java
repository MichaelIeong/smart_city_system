package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Getter;

import java.util.List;

@Getter
public class BadRequestException extends RuntimeException {

    public final ErrorResponse errorResponse;

    public BadRequestException(String errCode, String errMsg, List<ErrorResponse.ErrorDetail> errDetails) {
        super("BadRequestException: " + errCode + " - " + errMsg);
        this.errorResponse = new ErrorResponse(errCode, errMsg, errDetails);
    }

    public BadRequestException(String errCode, String errMsg, String location, String param, String msg) {
        this(errCode, errMsg, List.of(new ErrorResponse.ErrorDetail(location, param, msg)));
    }

    public BadRequestException(Exception e) {
        this("400", e.getMessage(), List.of());
    }

}
