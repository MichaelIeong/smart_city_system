package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.PropertyBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    public List<PropertyBriefResponse> findAllByProjectId(Integer projectId) {
        return propertyRepository.findByProjectInfoProjectId(projectId)
                .stream().map(PropertyBriefResponse::new)
                .toList();
    }
}
