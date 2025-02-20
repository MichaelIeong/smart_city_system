package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.DTO.ServiceBriefResponse;
import edu.fudan.se.sctap_lowcode_tool.model.ServiceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    public List<ServiceBriefResponse> findAllByProjectId(Integer projectId) {
        return serviceRepository.findAllByProjectId(projectId).stream().map(ServiceBriefResponse::new).toList();
    }

    public void addOrUpdateService(ServiceInfo serviceInfo){
        serviceRepository.save(serviceInfo);
    }

    public ServiceInfo getService(String serviceId){
        return serviceRepository.findByServiceId(serviceId);
    }

    public String generateCSP(String serviceId) {
        // 获取服务信息
        ServiceInfo serviceInfo = getService(serviceId);

        // 假设 serviceJson 包含了设备的 LHA 描述和场景描述
        String serviceJson = serviceInfo.getServiceJson();

        // serviceJson包含了组合服务里有哪些device，获取deviceIds
        ArrayList<String> lhaData = new ArrayList<>();


        // 构建要传给 LLM 的请求数据
        Map<String, ?> llmRequestData = Map.of(
                "sceneName", serviceInfo.getServiceName(),
                "devicesLHA", lhaData,
                "scenarioDescription", "这是一个 IoT 设备联动的场景，请用 线性混成自动机-CSP 描述该服务场景。" +
                        "我已经给出了每个设备的 lha 模型，请用CSP描述这些设备间的联动。最后请返回给我完整的线性混成自动机-CSP描述。"
        );
        // 调用 LLM API 生成 CSP 描述
        String csp = callLLMAPI(llmRequestData);
        // 将生成的 CSP 保存到数据库
        serviceInfo.setServiceCsp(csp);
        addOrUpdateService(serviceInfo);
        return csp;
    }

    // 调用 LLM API，生成 CSP
    private String callLLMAPI(Map<String, ?> requestData) {
        // LLM API URL，假设是一个 POST 请求
        String llmApiUrl = "https://llm.example.com/generateCSP";

        // 使用 RestTemplate 发送请求
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, ?>> entity = new HttpEntity<>(requestData, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                llmApiUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        // 从 LLM 返回的响应中提取 CSP
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();  // 假设返回的是 CSP 字符串
        } else {
            throw new RuntimeException("LLM API 请求失败: " + response.getStatusCode());
        }
    }
}
