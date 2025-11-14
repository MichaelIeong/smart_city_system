package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.EnvEvent;
import edu.fudan.se.sctap_lowcode_tool.repository.EnvEventRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class EnvEventService {
    @Resource
    private EnvEventRepository envEventRepository;

    public List<EnvEvent> findByGridId(String gridId) {
        return envEventRepository.findByGridId(gridId);
    }

    /**
     * 获取环境级事件列表
     * */
    public List<String> getEnvEventList(String gridId) {
        List<EnvEvent> envEvents = envEventRepository.findByGridId(gridId);
        return envEvents
                .stream()
                .map(EnvEvent::getEventJson)
                .toList();
    }

}
