package edu.fudan.se.sctap_lowcode_tool.DTO;

import edu.fudan.se.sctap_lowcode_tool.model.MeshInfo;
import lombok.Data;

import java.util.List;

public class SceneImportRequest {

    // 场景名称
    private String projectName;

    // 从 JSON 文件中解析出的网格数据列表
    private List<MeshInfo> meshes;

    // Getters and Setters (省略)
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<MeshInfo> getMeshes() {
        return meshes;
    }

    public void setMeshes(List<MeshInfo> meshes) {
        this.meshes = meshes;
    }
}