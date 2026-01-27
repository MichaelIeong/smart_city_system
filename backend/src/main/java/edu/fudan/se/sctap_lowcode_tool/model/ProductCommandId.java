package edu.fudan.se.sctap_lowcode_tool.model;
import lombok.Data;
import java.io.Serializable;

@Data
public class ProductCommandId implements Serializable {
    private String productId;
    private String commandId;
}