package edu.fudan.se.sctap_lowcode_tool.DTO;

import java.util.List;

public record PageDTO<T>(
        int pageNo,
        int pageSize,
        long totalCount,
        int totalPage,
        List<T> data
) {

}
