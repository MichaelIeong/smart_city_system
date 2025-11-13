package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.EnvProperty;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvPropertyRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class EnvPropertyService {
    @Resource
    private EnvPropertyRepository envPropertyRepository;

    public List<EnvProperty> findByGridId(String gridId) {
        return envPropertyRepository.findByGridId(gridId);
    }
}
