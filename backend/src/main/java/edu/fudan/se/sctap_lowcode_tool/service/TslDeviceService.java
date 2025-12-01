//package edu.fudan.se.sctap_lowcode_tool.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.http.*;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//@Service
//public class TslDeviceService {
//
//    @Value("${tsl.app.base-url}")
//    private String baseUrl;
//
//    @Value("${tsl.app.id}")
//    private String appId;
//
//    @Value("${tsl.app.code}")
//    private String appCode;
//
//    @Value("${tsl.app.token}")
//    private String token;
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    /**
//     * 生成 MD5 签名
//     */
//    private String md5Hex(String content) {
//        try {
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
//            StringBuilder sb = new StringBuilder();
//            for (byte b : digest) {
//                sb.append(String.format("%02x", b));
//            }
//            return sb.toString();
//        } catch (Exception e) {
//            throw new RuntimeException("MD5 加密失败", e);
//        }
//    }
//
//    /**
//     * 构造带签名的请求头
//     */
//    private HttpHeaders buildHeaders(String queryString) {
//        String timestamp = String.valueOf(System.currentTimeMillis());
//        String nonce = String.valueOf(new Random().nextInt(9999));
//        String signStr = queryString + appId + token + timestamp + nonce;
//        String sign = md5Hex(signStr);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("appId", appId);
//        headers.set("appCode", appCode);
//        headers.set("nonce", nonce);
//        headers.set("timestamp", timestamp);
//        headers.set("sign", sign);
//        headers.set("authorization", token);
//
//        return headers;
//    }
//
//    /**
//     * 调用接口B（/resource/page）获取设备所属区域 meshName
//     */
//    private String fetchDeviceRegion(String deviceId) {
//        String meshName = "未知区域";
//        try {
//            String queryString = "pageNum=1pageSize=1";
//            HttpHeaders headers = buildHeaders(queryString);
//
//            String url = baseUrl + "/resource/page?pageNum=1&pageSize=1";
//            Map<String, Object> payload = new HashMap<>();
//            payload.put("code", deviceId);
//            payload.put("directoryId", "5");
//
//            HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);
//            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, req, String.class);
//
//            Map<String, Object> resMap = objectMapper.readValue(res.getBody(), Map.class);
//            Map<String, Object> data = (Map<String, Object>) resMap.get("data");
//            if (data != null && data.get("datas") instanceof List) {
//                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("datas");
//                if (!list.isEmpty()) {
//                    Map<String, Object> extend = (Map<String, Object>) list.get(0).get("extend");
//                    if (extend != null && extend.get("meshName") != null) {
//                        meshName = extend.get("meshName").toString();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("获取设备区域失败（deviceId=" + deviceId + "）：" + e.getMessage());
//        }
//        return meshName;
//    }
//
//    /**
//     * 查询设备实例列表（整合接口A + 接口B）
//     */
//    public Map<String, Object> queryDeviceInstances(String prodId) {
//        try {
//            String queryString = "pageNum=1pageSize=100";
//            HttpHeaders headers = buildHeaders(queryString);
//
//            String url = baseUrl + "/device/dev/list/query?pageNum=1&pageSize=100";
//
//            Map<String, Object> payload = new HashMap<>();
//            payload.put("prodIds", List.of(prodId));
//            payload.put("projectId", "1001");
//
//            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
//            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
//
//            System.out.println("\n===== 调用接口A：设备列表 =====");
//            System.out.println("URL: " + url);
//            System.out.println("Payload: " + objectMapper.writeValueAsString(payload));
//            System.out.println("Status: " + response.getStatusCode());
//            System.out.println("=============================\n");
//
//            Map<String, Object> fullResp = objectMapper.readValue(response.getBody(), Map.class);
//            Map<String, Object> data = (Map<String, Object>) fullResp.get("data");
//
//            List<Map<String, Object>> datas = Collections.emptyList();
//            if (data != null && data.get("datas") instanceof List) {
//                List<Map<String, Object>> rawList = (List<Map<String, Object>>) data.get("datas");
//
//                // 查询 tsl_product 中的指令信息
//                String productInstruction = "";
//                try {
//                    String sql = "SELECT product_instruction FROM tsl_product WHERE product_id = ?";
//                    productInstruction = jdbcTemplate.queryForObject(sql, String.class, prodId);
//                    if (productInstruction != null && productInstruction.startsWith("[")) {
//                        productInstruction = productInstruction
//                                .replace("[", "")
//                                .replace("]", "")
//                                .replace("\"", "")
//                                .replace(",", "，");
//                    }
//                } catch (Exception e) {
//                    System.err.println("查询 tsl_product 指令失败：" + e.getMessage());
//                }
//                final String productOps = productInstruction;
//
//                // === 核心：并行调用接口B 获取 meshName ===
//                datas = rawList.parallelStream().map(d -> {
//                    String deviceId = String.valueOf(d.get("deviceId"));
//                    String meshName = fetchDeviceRegion(deviceId); // ✅ 接口B 查询区域名
//
//                    // 状态转换
//                    String statusLabel = "未知";
//                    Object sVal = d.get("status");
//                    if (sVal != null) {
//                        try {
//                            int s = Integer.parseInt(sVal.toString());
//                            statusLabel = (s == 1) ? "离线" : (s == 2) ? "在线" : "未知";
//                        } catch (Exception ignored) {}
//                    }
//
//                    // 时间格式化
//                    String deviceTime = "";
//                    Object tsVal = d.get("createTime");
//                    if (tsVal != null) {
//                        try {
//                            long ts = Long.parseLong(tsVal.toString());
//                            deviceTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(ts));
//                        } catch (Exception e) {
//                            deviceTime = tsVal.toString();
//                        }
//                    }
//
//                    Map<String, Object> devMap = new HashMap<>();
//                    devMap.put("deviceId", deviceId);
//                    devMap.put("deviceName", d.getOrDefault("name", ""));
//                    devMap.put("deviceTypeId", d.getOrDefault("prodId", ""));
//                    devMap.put("deviceTypeName", d.getOrDefault("prodName", ""));
//                    devMap.put("deviceRegion", meshName);
//                    devMap.put("states", List.of(Map.of("stateKey", "状态", "stateValue", statusLabel)));
//                    devMap.put("deviceTime", deviceTime);
//                    devMap.put("operation", productOps != null ? productOps : "无操作指令");
//
//                    return devMap;
//                }).toList();
//            }
//
//            return Map.of(
//                    "code", fullResp.getOrDefault("code", "00000"),
//                    "success", fullResp.getOrDefault("success", true),
//                    "message", fullResp.getOrDefault("message", "成功"),
//                    "data", datas
//            );
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Map.of("error", e.getMessage());
//        }
//    }
//}