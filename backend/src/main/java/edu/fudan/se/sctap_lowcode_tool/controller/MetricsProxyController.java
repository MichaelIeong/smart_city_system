package edu.fudan.se.sctap_lowcode_tool.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

@RestController
@RequestMapping("/api/metrics")
public class MetricsProxyController {

    // 注入配置文件中的参数
    @Value("${tsl.app.base-url}")
    private String remoteBaseUrl;

    @Value("${tsl.app.id}")
    private String appId;

    @Value("${tsl.app.code}")
    private String appCode;

    @Value("${tsl.app.token}")
    private String appToken;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 获取字典详情接口
     * 对应 Python: test_6_4_dict_info
     */
    @GetMapping("/dictInfo/detail/{id}")
    public Object proxyDictDetail(@PathVariable String id) {
        // 1. 准备请求 URL
        String path = "/metrics/dictInfo/detail/" + id;
        String fullUrl = remoteBaseUrl + path;

        // 2. 生成动态参数 (对应 Python: timestamp, nonce)
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = String.valueOf(new Random().nextInt(99999));

        // 3. 计算签名 (对应 Python: get_headers_and_url 中的逻辑)
        // 这个接口是 GET 请求且没有 Query String (?key=val)，所以 sign_query_str 为空字符串
        String signQueryStr = "";

        // 签名公式: MD5(sign_query_str + appId + token + timestamp + nonce)
        String signRaw = signQueryStr + appId + appToken + timestamp + nonce;
        String sign = md5Hex(signRaw);

        // 4. 构造 Headers (完全照搬 Python 代码的 headers 字典)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("appId", appId);
        headers.set("appCode", appCode);
        headers.set("nonce", nonce);
        headers.set("timestamp", timestamp);
        headers.set("sign", sign);
        headers.set("authorization", appToken); // Python里写的是 authorization: token

        // 5. 发起请求
        HttpEntity<String> entity = new HttpEntity<>(headers);

        System.out.println("正在转发请求: " + fullUrl);
        System.out.println("生成的签名: " + sign);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.GET,
                    entity,
                    Object.class
            );
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"success\":false, \"message\":\"请求转发失败: " + e.getMessage() + "\"}";
        }
    }

    /**
     * MD5 加密工具方法 (对应 Python: md5_hex)
     */
    private String md5Hex(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                // 转成 16 进制字符串，保持 2 位
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}