package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.repository.ActuatingFunctionDeviceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.ActuatingFunctionInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FunctionExeService {

    @Autowired
    ActuatingFunctionInfoRepository actuatingFunctionInfoRepository;

    @Autowired
    ActuatingFunctionDeviceRepository actuatingFunctionDeviceRepository;

    public Integer findIdByName(String name){
        return actuatingFunctionInfoRepository.findIdByName(name);
    }

    public String getApiUrl(Integer serviceId, String name){
        return actuatingFunctionDeviceRepository.findUrlByServiceIdAndName(serviceId, name);
    }
}
