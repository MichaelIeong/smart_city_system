package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.Data;

@Data
public class ProductCommandDTO {
    // 来自 product_function_command 表
    private String productId;
    private String functionId;
    private String functionName;
    private String commandId;
    private String commandName;

    // 来自 product_command_json 表
    private String commandJson;
}