package edu.fudan.se.sctap_lowcode_tool.controller;


import edu.fudan.se.sctap_lowcode_tool.DTO.ProductCommandDTO;
import edu.fudan.se.sctap_lowcode_tool.service.ProductFunctionCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/product_function")
public class ProductFunctionCommandController {

    @Autowired
    private ProductFunctionCommandService productFunctionCommandService;

    /**
     * 获取产品的所有指令信息（包含功能定义和JSON参数）
     * 请求示例: GET /api/product_function/commands?productId=p_ai_camera_tst
     */
    @GetMapping("/commands")
    public ResponseEntity<List<ProductCommandDTO>> getProductCommands(@RequestParam String productId) {
        List<ProductCommandDTO> list = productFunctionCommandService.getProductCommands(productId);

        return ResponseEntity.ok(list);
    }
}

