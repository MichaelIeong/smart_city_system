package edu.fudan.se.sctap_lowcode_tool.model;

import java.io.Serializable;
import java.util.Objects;

public class ProductEventId implements Serializable {
    private String productId;
    private String productEvent;

    public ProductEventId() {}

    public ProductEventId(String productId, String productEvent) {
        this.productId = productId;
        this.productEvent = productEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductEventId that = (ProductEventId) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(productEvent, that.productEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productEvent);
    }
}