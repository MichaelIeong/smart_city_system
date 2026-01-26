package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "product_event")
@IdClass(ProductEventId.class)
public class ProductEvent {

    @Id
    @Column(name = "product_id", length = 50)
    private String productId;

    @Id
    @Column(name = "product_event", length = 255)
    private String productEvent;

    @Column(name = "event_name")
    private String eventName;
}
