package edu.fudan.se.sctap_lowcode_tool.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductEventDTO {

    private String productEventId;   // 对应 product_event
    private String productEventName; // 对应 event_name
}