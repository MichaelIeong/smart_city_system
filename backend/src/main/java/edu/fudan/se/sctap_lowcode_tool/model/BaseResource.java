package edu.fudan.se.sctap_lowcode_tool.model;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class BaseResource {
    @Column(name = "description")
    private String description;

    @Column(name = "url")
    private String url;

    // 对应数据库中的 json 类型，JPA 层面映射为 String
    private String input;
    private String output;
}