package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product_command_json")
@IdClass(ProductCommandId.class)
public class ProductCommandJson {
    @Id
    @Column(name = "product_id")
    private String productId;

    @Id
    @Column(name = "command_id")
    private String commandId;

    @Column(name = "command_json")
    private String commandJson;
}
