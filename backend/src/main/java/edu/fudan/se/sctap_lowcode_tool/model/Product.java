package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tsl_product")
@Data
public class Product {
    @Id
    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_function")
    private String productFunction; // 存储 JSON 数组或单个字符串

    @Column(name = "product_json")
    private String productJson;     // 存储 JSON 对象字符串
}
