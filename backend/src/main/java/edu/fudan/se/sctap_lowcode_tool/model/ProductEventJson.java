package edu.fudan.se.sctap_lowcode_tool.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(name = "product_event_json")
public class ProductEventJson {

    @Id
    @Column(name = "product_event", length = 255)
    private String productEvent;

    // 直接映射为 Jackson 的 JsonNode，读取时自动转为 JSON 对象而不是字符串
    @Column(name = "event_json", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode eventJson;
}