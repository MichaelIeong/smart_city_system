package edu.fudan.se.sctap_lowcode_tool.DTO;

import java.util.List;

public record ErrorResponse(String errCode, String errMsg, List<ErrorDetail> errDetails) {
    public record ErrorDetail(String location, String param, String msg) {
    }
}
