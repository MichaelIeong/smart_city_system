package edu.fudan.se.sctap_lowcode_tool.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "grid_list")
@Data
public class GridMesh {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "mesh_no", length = 12)
    private String meshNo;

    @Column(name = "mesh_name", length = 10)
    private String meshName;

    @Column(name = "mesh_nature", length = 11)
    private String meshNature;

    @Column(name = "mesh_area")
    private Double meshArea;

    @Column(name = "mesh_type", length = 50)
    private String meshType;
}
