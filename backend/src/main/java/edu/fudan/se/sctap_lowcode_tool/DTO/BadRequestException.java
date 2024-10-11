package edu.fudan.se.sctap_lowcode_tool.DTO;

import java.util.List;

public class BadRequestException extends RuntimeException {

    public final ErrorResponse errorResponse;

    public BadRequestException(String errCode, String errMsg, List<ErrorDetail> errDetails) {
        super("BadRequestException: " + errCode + " - " + errMsg);
        this.errorResponse = new ErrorResponse(errCode, errMsg, errDetails);
    }

    public BadRequestException(String errCode, String errMsg, String location, String param, String msg) {
        this(errCode, errMsg, List.of(new ErrorDetail(location, param, msg)));
    }

    public ErrorResponse get() {
        return errorResponse;
    }

    public record ErrorDetail(String location, String param, String msg) {
    }

    public record ErrorResponse(String errCode, String errMsg, List<ErrorDetail> errDetails) {
    }
}
