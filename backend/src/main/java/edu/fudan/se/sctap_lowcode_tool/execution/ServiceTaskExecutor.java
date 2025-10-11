package edu.fudan.se.sctap_lowcode_tool.execution;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.model.ActuatingFunctionDevice;
import edu.fudan.se.sctap_lowcode_tool.model.DeviceInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.ActuatingFunctionDeviceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Component
public class ServiceTaskExecutor {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ActuatingFunctionDeviceRepository actuatingFunctionDeviceRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 执行 DeviceType 节点
     */
    public void executeDeviceTypeTask(JsonNode node, Integer spaceId) {
        Integer deviceTypeId = node.get("deviceType").asInt();
        Integer functionId = node.get("deviceService").asInt();

        // 1. 找到该空间下符合 deviceTypeId 的具体设备
        List<DeviceInfo> devices = deviceRepository.findBySpaceSpaceIdAndDeviceTypeId(spaceId, deviceTypeId);
        if (devices.isEmpty()) {
            throw new RuntimeException("空间 " + spaceId + " 下没有符合类型 " + deviceTypeId + " 的设备");
        }

        // 👉 策略：这里简单起见，先取第一个
        DeviceInfo device = devices.get(0);

        // 2. 找到该设备的所有 actuating functions
        List<ActuatingFunctionDevice> funcs = actuatingFunctionDeviceRepository.findByDevice_Id(device.getId());

        // 3. 匹配方法类型 id
        Optional<ActuatingFunctionDevice> funcMatch = funcs.stream()
                .filter(f -> f.getActuatingFunction().getId().equals(functionId))
                .findFirst();

        if (funcMatch.isEmpty()) {
            throw new RuntimeException("设备 " + device.getDeviceName() + " 下没有找到方法 " + functionId);
        }

        // 4. 拿到具体 url 去执行
        String url = funcMatch.get().getUrl();
        System.out.println("执行设备 " + device.getDeviceName() +
                " 的方法 " + funcMatch.get().getActuatingFunction().getName() +
                "，调用 URL = " + url);

        // 5. 真正调用 REST API
        try {
            String result = restTemplate.getForObject(url, String.class);
            System.out.println("调用结果: " + result);
        } catch (Exception e) {
            throw new RuntimeException("调用设备 API 失败: " + url, e);
        }
    }

    public void executeSocialServiceTask(JsonNode node, Integer spaceId) {
        String resourceType = node.get("resourceType").asText();
        String resourceId = node.get("socialResource").asText();
        String label = node.get("socialResourceLabel").asText();
        String func = node.get("func").asText();

        System.out.println("执行 Social Service: " + label +
                " (资源类型: " + resourceType + ", 资源ID: " + resourceId +
                ", API: " + func + ", spaceId: " + spaceId + ")");
        // 拼接 url，如果 func 不是完整路径，可以加上 baseUrl
        String baseUrl = "http://localhost:8080/"; // TODO: 改成实际 API 网关
        String url = func.startsWith("http") ? func : baseUrl + func;

        try {
            String result = restTemplate.getForObject(url, String.class);
            System.out.println("执行 Information Service 调用成功: " + url + " -> " + result);
        } catch (Exception e) {
            throw new RuntimeException("执行 Information Service 调用失败: " + url, e);
        }
    }

    public void executeInformationServiceTask(JsonNode node, Integer spaceId) {
        String resourceType = node.get("resourceType").asText();
        String resourceId = node.get("informationResource").asText();
        String label = node.get("informationResourceLabel").asText();
        String func = node.get("func").asText();

        System.out.println("执行 Information Service: " + label +
                " (资源类型: " + resourceType + ", 资源ID: " + resourceId +
                ", API: " + func + ", spaceId: " + spaceId + ")");
        // 拼接 url，如果 func 不是完整路径，可以加上 baseUrl
        String baseUrl = "http://localhost:8080/"; // TODO: 改成实际 API 网关
        String url = func.startsWith("http") ? func : baseUrl + func;

        try {
            String result = restTemplate.getForObject(url, String.class);
            System.out.println("执行 Information Service 调用成功: " + url + " -> " + result);
        } catch (Exception e) {
            throw new RuntimeException("执行 Information Service 调用失败: " + url, e);
        }
    }
}