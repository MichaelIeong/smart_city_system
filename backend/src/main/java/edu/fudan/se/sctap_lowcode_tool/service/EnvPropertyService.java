package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.EnvProperty;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvPropertyRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EnvPropertyService {
    @Resource
    private EnvPropertyRepository envPropertyRepository;

    public List<EnvProperty> findByGridId(String gridId) {
        return envPropertyRepository.findByGridId(gridId);
    }

    /**
     * 获取环境级属性列表
     * */
    public List<String> getEnvPropertyList(String gridId) {
        List<EnvProperty> envProperties = findByGridId(gridId);
        return envProperties.stream()
                .map(envProperty -> envProperty.getPropertyName() + ": " + envProperty.getDescription())
                .collect(Collectors.toList());
    }
}
