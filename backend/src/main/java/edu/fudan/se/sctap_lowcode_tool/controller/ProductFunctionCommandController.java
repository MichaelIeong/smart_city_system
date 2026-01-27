package edu.fudan.se.sctap_lowcode_tool.controller;


import edu.fudan.se.sctap_lowcode_tool.DTO.ProductCommandDTO;
import edu.fudan.se.sctap_lowcode_tool.service.ProductFunctionCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/product_function")
public class ProductFunctionCommandController {

    @Autowired
    private ProductFunctionCommandService productFunctionCommandService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取产品的所有指令信息（包含功能定义和JSON参数）
     * 请求示例: GET /api/product_function/commands?productId=p_ai_camera_tst
     */
    @GetMapping("/commands")
    public ResponseEntity<List<ProductCommandDTO>> getProductCommands(@RequestParam String productId) {
        List<ProductCommandDTO> list = productFunctionCommandService.getProductCommands(productId);

        return ResponseEntity.ok(list);
    }

    @GetMapping("/functions")
    public List<String> getProductFunctions(@RequestParam String typeId){
        return productFunctionCommandService.getFunctionsByProductId(typeId);
    }


    public List<String> getCommandsByFunctions(@RequestParam String functionName){
        return productFunctionCommandService.getCommandsByFunctionName(functionName);
    }

    @GetMapping("/f-commands")
    public List<Map<String, String>> getCommandJsonByFunctionName(String functionName) {
        List<Map<String, String>> result = new ArrayList<>();

        try {
            // Step 1: 根据 functionName 从 product_function_command 表查询 command_id
            // 注意：使用 DISTINCT 去重
            String sql1 = "SELECT DISTINCT command_id FROM product_function_command WHERE function_name = ?";
            List<String> commandIds = jdbcTemplate.queryForList(sql1, String.class, functionName);

            // 如果没有查到数据，直接返回空列表
            if (commandIds.isEmpty()) {
                return result;
            }

            // Step 2: 根据 functionName 列表从 product_function_command 表查询 command_name
            // 构建 IN 查询的占位符 (?, ?, ?...)
            String inClause = String.join(",", Collections.nCopies(commandIds.size(), "?"));
            String sql2 = "SELECT command_id, command_name FROM product_function_command WHERE command_id IN (" + inClause + ")";

            // 执行查询
            List<Map<String, Object>> commandDetails = jdbcTemplate.queryForList(sql2, commandIds.toArray());

            // Step 3: 组装数据。前端下拉框通常需要 value 和 label
            for (Map<String, Object> row : commandDetails) {
                Map<String, String> option = new HashMap<>();
                // value 通常存 product_id (用于后台逻辑)
                option.put("value", row.get("command_id").toString());
                // label 通常存 product_name (用于前端显示)
                option.put("label", row.get("command_name").toString());
                result.add(option);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 建议记录日志 logger.error("查询设备类型失败", e);
        }

        return result;
    }

    @GetMapping("/c-commandJson")
    public String getCommandJson(@RequestParam String commandId){
        return productFunctionCommandService.getCommandJsonById(commandId);
    }


}

