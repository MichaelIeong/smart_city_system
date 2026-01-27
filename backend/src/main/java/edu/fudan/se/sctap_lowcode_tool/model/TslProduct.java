package edu.fudan.se.sctap_lowcode_tool.model;

import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "tsl_product")
public class TslProduct {

    @Id
    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_describe")
    private String productDescribe;

    @Column(name = "product_function")
    private String productFunction;

    @Column(name = "product_property")
    private String productProperty;

    @Column(name = "product_instruction")
    private String productInstruction;

    @Column(name = "product_event")
    private String productEvent;
    @Column(name = "product_json")
    private String productJson;

    @Column(name = "action_name")
    private String actionName;

    @Column(name = "project_id")
    private Integer projectId;
}