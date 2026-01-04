package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.MeshInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.MeshRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeshService {

    @Autowired
    private MeshRepository meshRepository;

    public MeshInfo save(MeshInfo mesh) {
        return meshRepository.save(mesh);
    }

    public List<MeshInfo> findByProjectId(Integer id) {
        return meshRepository.findByProjectId(id);
    }

    public void deleteByProjectId(Integer id) {
        meshRepository.deleteByProjectId(id);
    }
}