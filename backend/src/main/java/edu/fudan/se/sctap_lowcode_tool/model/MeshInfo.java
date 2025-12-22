package edu.fudan.se.sctap_lowcode_tool.model;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "mesh_info")
@Data
public class MeshInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String meshCode;

    private String meshName;

    private String meshType;

    @Transient // 告诉 JPA 忽略此字段，仅用于 Jackson 接收 JSON
    private List<MeshGridPoint> meshGridList;

    @Column(name = "mesh_grid_list", columnDefinition = "text") // 数据库存储字段
    private String meshGridListJson; // 【新增】用于存储序列化后的 JSON 字符串

    private Integer projectId;
}