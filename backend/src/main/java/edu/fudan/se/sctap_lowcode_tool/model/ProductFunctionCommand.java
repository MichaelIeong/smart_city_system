package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product_function_command")
@IdClass(ProductCommandId.class)
public class ProductFunctionCommand {

    @Id
    @Column(name = "product_id")
    private String productId;

    @Id
    @Column(name = "command_id")
    private String commandId;

    @Column(name = "function_id")
    private String functionId;

    @Column(name = "function_name")
    private String functionName;

    @Column(name = "command_name")
    private String commandName;

    /**
     * 定义一对一关联 (对应 LEFT JOIN)
     * 使用 @JoinColumns 映射两个关联字段
     * insertable/updatable = false 表示该字段只用于查询关联，不参与修改主表的外键
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "product_id", referencedColumnName = "product_id", insertable = false, updatable = false),
            @JoinColumn(name = "command_id", referencedColumnName = "command_id", insertable = false, updatable = false)
    })
    private ProductCommandJson commandJsonDetail;
}