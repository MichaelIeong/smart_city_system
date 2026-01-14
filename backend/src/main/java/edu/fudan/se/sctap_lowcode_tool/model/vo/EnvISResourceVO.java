package edu.fudan.se.sctap_lowcode_tool.model.vo;

import lombok.Data;

@Data
public class EnvISResourceVO {
    private String resourceName; // 对应数据库的 description
    private String resourceUrl;  // 对应数据库的 url
    private Object input;        // 自动解析为 List<Map>
    private Object output;       // 自动解析为 Map
}
