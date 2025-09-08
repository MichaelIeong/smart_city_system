package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.EnvServiceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.ProjectInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvServiceInfoRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvServiceInfoService {

    private final EnvServiceInfoRepository envServiceInfoRepository;
    private final ProjectRepository projectRepository;

    public EnvServiceInfo create(String envServiceName, Integer projectId) {
        ProjectInfo project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));

        EnvServiceInfo entity = new EnvServiceInfo();
        entity.setEnvServiceName(envServiceName);
        entity.setProject(project);
        return envServiceInfoRepository.save(entity);
    }

    public EnvServiceInfo getById(Integer id) {
        return envServiceInfoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EnvService not found: " + id));
    }

    public List<EnvServiceInfo> listAll() {
        return envServiceInfoRepository.findAll();
    }

    public List<EnvServiceInfo> listByProject(Integer projectId) {
        ProjectInfo project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));
        return envServiceInfoRepository.findByProject(project);
    }

    public EnvServiceInfo update(Integer id, String envServiceName, Integer projectId) {
        EnvServiceInfo entity = envServiceInfoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EnvService not found: " + id));

        if (envServiceName != null) {
            entity.setEnvServiceName(envServiceName);
        }
        if (projectId != null) {
            ProjectInfo project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));
            entity.setProject(project);
        }
        return envServiceInfoRepository.save(entity);
    }

    public void delete(Integer id) {
        if (!envServiceInfoRepository.existsById(id)) {
            throw new EntityNotFoundException("EnvService not found: " + id);
        }
        envServiceInfoRepository.deleteById(id);
    }
}