package edu.fudan.se.sctap_lowcode_tool.service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import edu.fudan.se.sctap_lowcode_tool.utils.SignUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class SceneService {

    @Value("${tsl.app.base-url}")
    private String baseUrl;

    @Value("${tsl.app.id}")
    private String appId;

    @Value("${tsl.app.code}")
    private String appCode;

    @Value("${tsl.app.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> fetchAndParseGridData(String meshNature) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 准备时间戳和随机数 (对应 Python 的 time 和 random)
            String timestamp = String.valueOf(System.currentTimeMillis());
            // Python: random.randint(0, 99999)
            String nonce = String.valueOf(new Random().nextInt(100000));

            // 2. 计算签名 (针对无 Query Param 的情况)
            String sign = SignUtil.calculateSignature(appId, token, timestamp, nonce);

            // 3. 构造 Headers (严格对应 Python 代码中的 headers 字典)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("appId", appId);
            headers.add("appCode", appCode);
            headers.add("nonce", nonce);
            headers.add("timestamp", timestamp);
            headers.add("sign", sign);
            headers.add("authorization", token); // Python 中 token 也是 authorization

            // 4. 构造 Body 参数
            Map<String, Object> bodyParams = new HashMap<>();
            bodyParams.put("meshNature", meshNature); // 例如 "F-city"
            bodyParams.put("pageNum", 1);
            bodyParams.put("pageSize", 20);

            // 5. 发起 POST 请求
            String targetUrl = baseUrl + "/metrics/meshInfo/page";
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(bodyParams, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, requestEntity, String.class);

            // 6. 解析响应
            JSONObject jsonResponse = JSON.parseObject(response.getBody());

            // 检查 success 字段
            if (jsonResponse != null && jsonResponse.getBooleanValue("success")) {
                JSONObject dataObj = jsonResponse.getJSONObject("data");
                if (dataObj != null) {
                    JSONArray datas = dataObj.getJSONArray("datas");

                    result.put("success", true);
                    result.put("data", datas);
                    result.put("message", "OK");
                    return result;
                }
            }

            // 逻辑失败
            result.put("success", false);
            result.put("message", "接口返回非成功状态");

        } catch (Exception e) {
            // 异常捕获 (包括网络错误、签名错误等)
            // 根据需求：不向前端暴露具体错误
            e.printStackTrace(); // 仅后端日志
            result.put("success", false);
            result.put("message", "系统内部错误");
        }

        return result;
    }
}
