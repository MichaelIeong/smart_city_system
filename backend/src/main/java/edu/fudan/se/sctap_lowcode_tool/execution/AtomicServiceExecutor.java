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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.transaction.annotation.Transactional;


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
import java.util.concurrent.TimeUnit;

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
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    
    public String executeCyber(JsonNode stepNode, String location, Map<String, Object> finalArgs) {
        String nodeName = stepNode.path("name").asText();

        try {
            // 工单类 cyber：走工单创建接口
            if (isOrderLikeCyber(stepNode)) {
                Map<String, Object> responseMap = createOrder(location, finalArgs);

                System.out.println("[网络下发-新工单创建] 节点: " + nodeName + " | 响应: " + responseMap);

                boolean success = responseMap != null
                        && Boolean.TRUE.equals(responseMap.get("success"))
                        && "00000".equals(String.valueOf(responseMap.get("code")));

                if (success) {
                    return formatLog("INFO",
                            String.format("网络资源 [%s] 工单创建成功，工单ID: %s，响应: %s",
                                    nodeName, responseMap.get("data"), responseMap));
                } else {
                    return formatLog("ERROR",
                            String.format("网络资源 [%s] 工单创建失败，响应: %s",
                                    nodeName, responseMap));
                }
            }

            // 普通 cyber：走配置的 apiUrl
            List<CyberResourceInfo> resources = cyberResourceRepository.findByResourceType(nodeName);
            if (CollectionUtils.isEmpty(resources)) {
                return formatLog("ERROR", "未找到网络资源配置: " + nodeName);
            }

            CyberResourceInfo resource = resources.get(0);
            String apiUrl = resource.getUrl();

            Map<String, Object> responseMap = restTemplate.postForObject(apiUrl, finalArgs, Map.class);

            System.out.println("[网络下发] 地址: " + apiUrl + " | 响应内容: " + responseMap);

            if (responseMap != null && Boolean.TRUE.equals(responseMap.get("result"))) {
                return formatLog("INFO", String.format("网络资源 [%s] 下发成功，外部响应: %s", nodeName, responseMap));
            } else {
                return formatLog("ERROR", String.format("网络资源 [%s] 下发业务返回失败，响应: %s", nodeName, responseMap));
            }

        } catch (Exception e) {
            System.err.println("[网络下发异常] 节点: " + nodeName + " | 原因: " + e.getMessage());
            return formatLog("ERROR", "网络资源下发过程中出现异常: " + e.getMessage());
        }
    }

    private boolean isOrderLikeCyber(JsonNode stepNode) {
        String name = stepNode.path("name").asText("");
        return name.contains("工单");
    }

    private Map<String, Object> createOrder(String location, Map<String, Object> finalArgs) {
        String orderUrl = "http://60.161.136.138:32014//metrics/workOrder/add";

        Map<String, Object> bodyParam = new HashMap<>();

        String orderName = firstNonEmpty(
                getString(finalArgs.get("orderName")),
                getString(finalArgs.get("工单名称"))
        );

        String address = firstNonEmpty(
                getString(finalArgs.get("address")),
                getString(finalArgs.get("准确地址"))
        );

        String assignTarget = "{\"orgId\":\"fudan\",\"roleId\":\"\",\"userId\":[\"01f61be9b332490983542b986e8f06c6\"],\"deptApp\":\"\"}";

        bodyParam.put("address", address);
        bodyParam.put("assignTarget", assignTarget);
        bodyParam.put("deviceRef", "");
        bodyParam.put("dutyNetwork", getString(location));
        bodyParam.put("fromEvent", "");
        bodyParam.put("latitude", null);
        bodyParam.put("longitude", null);
        bodyParam.put("orderName", orderName);
        bodyParam.put("orderType", "intside");
        bodyParam.put("photos", "");
        bodyParam.put("remarks", "");
        bodyParam.put("validTime", "one_day");

        System.out.println("[新工单创建请求体] " + bodyParam);

        // --- 计算签名与 Headers（复用 executePhysical 的鉴权逻辑） ---
        String appId = "6bdece1382b5488a";
        String appCode = "Ubiquitous-OS";
        String token = "ZGIWMJHKMWZHZMVMNDAYYZLLMMU5YZE0MZU0YWFJYZFMNTYZNTDJODQ5ZJQ0OTG0";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = String.valueOf(new Random().nextInt(5)); // 如需更稳可换成 UUID
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

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(bodyParam, headers);

        // ===== 打印请求信息 =====
        System.out.println("========== 工单创建请求开始 ==========");
        System.out.println("URL: " + orderUrl);
        System.out.println("Headers: " + headers);
        System.out.println("Body: " + bodyParam);
        System.out.println("signStr: " + signStr);
        System.out.println("sign: " + sign);
        System.out.println("========== 工单创建请求结束 ==========");

        Map<String, Object> responseMap = restTemplate.postForObject(orderUrl, requestEntity, Map.class);
        System.out.println("[新工单创建响应] " + responseMap);

        return responseMap;
    }

    private String getString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v;
            }
        }
        return "";
    }    

    public String executeSocial(JsonNode stepNode,Map<String, Object> finalArgs) {
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

    public String executePhysical(JsonNode stepNode, String location, Map<String, Object> finalArgs) {
        String nodeName = stepNode.path("name").asText();
        String actionName = stepNode.path("action").asText();
        String cmdName = stepNode.path("command").asText();

        Long deviceId = null; // 提前定义，方便成功/异常日志都能拿到

        try {
            TslProduct product = tslProductRepository.findByProductName(nodeName);
            if (product == null) return formatLog("ERROR", "未知产品: " + nodeName);
            String productId = product.getProductId();

            List<TslDevice> devices = tslDeviceRepository.findByProductProductIdAndMeshId(productId, location);
            System.out.println("地址: " + location);
            System.out.println("productId: " + productId);
            if (CollectionUtils.isEmpty(devices)) return formatLog("WARN", "区域内无可用设备: " + nodeName);

            deviceId = devices.get(0).getDeviceId();

            // 状态变更逻辑 (2 -> 3) ---
            tslDeviceRepository.updateStatusById(deviceId, 3);
            System.out.println("设备状态已修改为 3, deviceId: " + deviceId);

            // 开启定时任务，30秒后还原 (3 -> 2) ---
            final Long finalDeviceId = deviceId; // 匿名内部类需要 final
            scheduler.schedule(() -> {
                try {
                    tslDeviceRepository.updateStatusById(finalDeviceId, 2);
                    System.out.println("30秒到，设备状态已还原为 2, deviceId: " + finalDeviceId);
                } catch (Exception e) {
                    System.err.println("还原设备状态失败: " + e.getMessage());
                }
            }, 30, TimeUnit.SECONDS);

            ProductFunctionCommand cmdEntity = productCommandRepository
                    .findByProductIdAndFunctionNameAndCommandName(productId, actionName, cmdName);
            if (cmdEntity == null) return formatLog("ERROR", "指令映射失败: " + cmdName);

            // 载荷过滤组装
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

            return formatLog(
                    "INFO",
                    String.format(
                            "物理设备 [%s] 下发成功! deviceId: %s, 指令: %s, 响应: %s",
                            nodeName, deviceId, cmdName, response.getBody()
                    )
            );

        } catch (Exception e) {
            return formatLog(
                    "ERROR",
                    String.format(
                            "物理执行异常: deviceId=%s, error=%s",
                            deviceId, e.getMessage()
                    )
            );
        }
    }
}