package edu.fudan.se.sctap_lowcode_tool.execution;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.digest.DigestUtils;
import edu.fudan.se.sctap_lowcode_tool.model.CyberResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.ProductCommandJson;
import edu.fudan.se.sctap_lowcode_tool.model.ProductFunctionCommand;
import edu.fudan.se.sctap_lowcode_tool.model.SocialResourceInfo;
import edu.fudan.se.sctap_lowcode_tool.model.TslDevice;
import edu.fudan.se.sctap_lowcode_tool.model.TslProduct;
import edu.fudan.se.sctap_lowcode_tool.repository.SocialResourceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.TslDeviceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.TslProductRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.CyberResourceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.ProductCommandRepository;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Arrays; 
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper; 
import com.fasterxml.jackson.databind.JsonNode;     

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 原子服务执行器：负责执行工作流中最小单元的具体业务逻辑
 */
@Component
public class AtomicServiceExecutor {
    @Autowired
    private SocialResourceRepository socialResourceRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TslProductRepository tslProductRepository;
    @Autowired
    private CyberResourceRepository cyberResourceRepository;
    @Autowired
    private TslDeviceRepository tslDeviceRepository;
    @Autowired
    private ProductCommandRepository productCommandRepository;
    private String formatLog(String level, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss"));
        return String.format("[%s]-[%s]: %s", level, timestamp, message);
    }
    public String executeCyber(JsonNode stepNode, Map<String, Object> finalArgs) {
        String nodeName = stepNode.path("name").asText();
        
        try {
            // 核心改动：使用 cyberResourceRepository 查询资源配置
            List<CyberResourceInfo> resources = cyberResourceRepository.findByResourceType(nodeName);
            if (CollectionUtils.isEmpty(resources)) {
                return formatLog("ERROR", "未找到网络资源配置: " + nodeName);
            }
            
            CyberResourceInfo resource = resources.get(0);
            String apiUrl = resource.getUrl();

            // 1. 发起网络请求
            Map<String, Object> responseMap = restTemplate.postForObject(apiUrl, finalArgs, Map.class);
            
            // 2. 打印日志
            System.out.println("[网络下发] 地址: " + apiUrl + " | 响应内容: " + responseMap);

            // 3. 业务逻辑判断 (响应不为空且 result 字段为 true)
            if (responseMap != null && Boolean.TRUE.equals(responseMap.get("result"))) {
                return formatLog("INFO", String.format("网络资源 [%s] 下发成功，外部响应: %s", nodeName, responseMap));
            } else {
                return formatLog("ERROR", String.format("网络资源 [%s] 下发业务返回失败，响应: %s", nodeName, responseMap));
            }

        } catch (Exception e) {
            // 捕获网络异常、超时等
            System.err.println("[网络下发异常] 节点: " + nodeName + " | 原因: " + e.getMessage());
            return formatLog("ERROR", "网络资源下发过程中出现异常: " + e.getMessage());
        }
    }

    public String executeSocial(JsonNode stepNode, Map<String, Object> finalArgs) {
        String nodeName = stepNode.path("name").asText();
        
        try {
            List<SocialResourceInfo> resources = socialResourceRepository.findByResourceType(nodeName);
            if (CollectionUtils.isEmpty(resources)) {
                return formatLog("ERROR", "未找到资源配置: " + nodeName);
            }
            
            SocialResourceInfo resource = resources.get(0);
            String apiUrl = resource.getUrl();

            // 1. 直接用 Map 接收响应，RestTemplate 会自动处理 JSON 到 Map 的转换
            Map<String, Object> responseMap = restTemplate.postForObject(apiUrl, finalArgs, Map.class);
            
            // 2. 打印日志方便控制台观察（注意：responseMap 打印出来是 {result=true} 这种格式）
            System.out.println("[真实下发] 地址: " + apiUrl + " | 响应内容: " + responseMap);

            // 3. 业务逻辑判断
            // 判断标准：HTTP 请求成功 且 响应中的 result 字段为 true
            if (responseMap != null && Boolean.TRUE.equals(responseMap.get("result"))) {
                return formatLog("INFO", String.format("社会资源 [%s] 下发成功，外部响应: %s", nodeName, responseMap));
            } else {
                // 如果 result 为 false 或 response 为空，按失败处理
                return formatLog("ERROR", String.format("社会资源 [%s] 下发业务返回失败，响应: %s", nodeName, responseMap));
            }

        } catch (Exception e) {
            // 捕获：网络超时、404、500、连接被拒绝等异常
            System.err.println("[下发异常] 节点: " + nodeName + " | 原因: " + e.getMessage());
            return formatLog("ERROR", "社会资源下发过程中出现网络或系统异常: " + e.getMessage());
        }
    }

    public String executePhysical(JsonNode stepNode, Map<String, Object> finalArgs) {
        String nodeName = stepNode.path("name").asText();      
        String actionName = stepNode.path("action").asText();  
        String cmdName = stepNode.path("command").asText();    
        String location = (String) finalArgs.get("location");  

        try {
            TslProduct product = tslProductRepository.findByProductName(nodeName);
            if (product == null) return formatLog("ERROR", "未知产品: " + nodeName);
            String productId = product.getProductId();

            List<TslDevice> devices = tslDeviceRepository.findByProductProductIdAndMeshId(productId, location);
            if (CollectionUtils.isEmpty(devices)) return formatLog("WARN", "区域内无可用设备: " + nodeName);
            Long deviceId = devices.get(0).getDeviceId();

            ProductFunctionCommand cmdEntity = productCommandRepository
                    .findByProductIdAndFunctionNameAndCommandName(productId, actionName, cmdName);
            if (cmdEntity == null) return formatLog("ERROR", "指令映射失败: " + cmdName);

            //载荷过滤组装
            ProductCommandJson jsonDetail = cmdEntity.getCommandJsonDetail();
            Map<String, Object> realPayload = new HashMap<>();
            if (jsonDetail != null && jsonDetail.getCommandJson() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode schemaNode = mapper.readTree(jsonDetail.getCommandJson());
                for (JsonNode item : schemaNode) {
                    String key = item.path("key").asText();
                    if (finalArgs.containsKey(key)) {
                        realPayload.put(key, finalArgs.get(key));
                    }
                }
            }

            // --- 3. 封装 API 特定的 body 结构 ---
            Map<String, Object> cmdPart = new HashMap<>();
            cmdPart.put("function", cmdEntity.getFunctionId());
            cmdPart.put("command", cmdEntity.getCommandId());
            cmdPart.put("param", realPayload);

            Map<String, Object> bodyParam = new HashMap<>();
            bodyParam.put("cmd", cmdPart);
            bodyParam.put("deviceId", deviceId.toString()); // 对方 API 要求 String 类型的 ID

            System.out.println("body: " + bodyParam);

            // --- 4. 计算签名与 Headers (Python 鉴权逻辑) ---
            String appId = "6bdece1382b5488a";
            String appCode = "Ubiquitous-OS";
            String token = "ZGIWMJHKMWZHZMVMNDAYYZLLMMU5YZE0MZU0YWFJYZFMNTYZNTDJODQ5ZJQ0OTG0";
            String timestamp = String.valueOf(System.currentTimeMillis());
            String nonce = String.valueOf(new Random().nextInt(5)); // 0-4

            // 签名逻辑：MD5(appId + token + timestamp + nonce)
            String signStr = appId + token + timestamp + nonce;
            String sign = org.apache.commons.codec.digest.DigestUtils.md5Hex(signStr);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("appId", appId);
            headers.set("appCode", appCode);
            headers.set("nonce", nonce);
            headers.set("timestamp", timestamp);
            headers.set("sign", sign);
            headers.set("authorization", token);

            // --- 5. 真实请求下发 ---
            String deviceUrl = "http://60.161.136.138:32014/device/api/device/send/command";
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(bodyParam, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(deviceUrl, requestEntity, String.class);
            System.out.println("响应: " + response);

            return formatLog("INFO", String.format("物理设备 [%s] 下发成功! 指令: %s, 响应: %s", 
                    nodeName, cmdName, response.getBody()));

        } catch (Exception e) {
            return formatLog("ERROR", "物理执行异常: " + e.getMessage());
        }
    }
}