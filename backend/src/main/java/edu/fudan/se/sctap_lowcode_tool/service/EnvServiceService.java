package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.EnvService;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvServiceRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class EnvServiceService {
    @Resource
    private EnvServiceRepository envServiceRepository;

    public List<EnvService> findByGridId(String gridId) {
        return envServiceRepository.findByGridId(gridId);
    }
}
