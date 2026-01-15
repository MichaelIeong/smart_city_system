package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tsl_product")
public class TslProduct {

    /**
     * 产品 ID (主键，如 p_ai_camera_tst)
     */
    @Id
    @Column(name = "product_id", nullable = false)
    private String productId;

    /**
     * 产品名称 (如 AI摄像机)
     */
    @Column(name = "product_name")
    private String productName;

    /**
     * 产品功能描述 (如 AI摄像机事件)
     */
    @Column(name = "product_function")
    private String productFunction;

    /**
     * 产品指令集 (对应 product_instruction)
     * JSON 字符串格式，Service 层会解析它
     */
    @Column(name = "product_instruction")
    private String productInstruction;

    /**
     * 产品描述 (对应 product_describe)
     */
    @Column(name = "product_describe")
    private String productDescribe;

    // 如果数据库里还有 product_property, product_event 等字段，也可以按需添加
}