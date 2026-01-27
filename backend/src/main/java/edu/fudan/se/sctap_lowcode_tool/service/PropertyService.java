package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.PropertyBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.repository.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
