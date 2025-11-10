package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
public class TslDeviceService {

    @Value("${tsl.app.base-url}")
    private String baseUrl;

    @Value("${tsl.app.id}")
    private String appId;

    @Value("${tsl.app.code}")
    private String appCode;

    @Value("${tsl.app.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成 MD5 签名
     */
    private String md5Hex(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 加密失败", e);
        }
    }

    /**
     * 构造带签名的请求头
     */
    private HttpHeaders buildHeaders(String queryString) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = String.valueOf(new Random().nextInt(9999));
        String signStr = queryString + appId + token + timestamp + nonce;
        String sign = md5Hex(signStr);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("appId", appId);
        headers.set("appCode", appCode);
        headers.set("nonce", nonce);
        headers.set("timestamp", timestamp);
        headers.set("sign", sign);
        headers.set("authorization", token);

        return headers;
    }

    /**
     * 查询设备实例列表（调用外部 TSL 平台接口）
     * @param prodId 设备类型 ID（例如 p_ai_camera_tst）
     */
    public Map<String, Object> queryDeviceInstances(String prodId) {
        try {
            String queryString = "pageNum=1pageSize=100";
            HttpHeaders headers = buildHeaders(queryString);

            String url = baseUrl + "/device/dev/list/query?pageNum=1&pageSize=100";

            Map<String, Object> payload = new HashMap<>();
            payload.put("prodIds", List.of(prodId));
            payload.put("projectId", "1001"); // ⚠️ 请根据你的实际项目调整

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            // 解析返回 JSON
            Map<String, Object> fullResp = objectMapper.readValue(response.getBody(), Map.class);
            Map<String, Object> data = (Map<String, Object>) fullResp.get("data");

            List<Map<String, Object>> datas = Collections.emptyList();
            if (data != null && data.get("datas") instanceof List) {
                List<Map<String, Object>> rawList = (List<Map<String, Object>>) data.get("datas");

                // ✅ 映射成前端期望的字段结构
                datas = rawList.stream().map(d -> Map.of(
                        "deviceId", d.getOrDefault("deviceId", ""),
                        "deviceName", d.getOrDefault("name", ""), // 映射 name → deviceName
                        "deviceTypeId", d.getOrDefault("prodId", ""),
                        "deviceTypeName", d.getOrDefault("prodName", ""),
                        "deviceRegion", d.getOrDefault("spaceCode", "未知区域"),
                        "states", List.of(Map.of("stateKey", "status", "stateValue", d.getOrDefault("status", ""))),
                        "deviceTime", d.getOrDefault("createTime", ""),
                        "operation", d.getOrDefault("protocol", "")
                )).toList();
            }

            // ✅ 统一返回结构
            return Map.of(
                    "code", fullResp.getOrDefault("code", "00000"),
                    "success", fullResp.getOrDefault("success", true),
                    "message", fullResp.getOrDefault("message", "成功"),
                    "data", datas
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }
}