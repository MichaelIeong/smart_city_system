package edu.fudan.se.sctap_lowcode_tool.service;
import edu.fudan.se.sctap_lowcode_tool.utils.SignUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.*;

@Service
public class GridService {

    @Value("${tsl.app.base-url}")
    private String baseUrl;

    @Value("${tsl.app.id}")
    private String appId;

    @Value("${tsl.app.code}")
    private String appCode;

    @Value("${tsl.app.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    /** 构造签名头 */
    private HttpHeaders buildHeaders(String queryString) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = String.valueOf(new Random().nextInt(9999));
        String signStr = (queryString != null ? queryString : "") + appId + token + timestamp + nonce;
        String sign = SignUtil.md5Hex(signStr);

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

    /** 统一封装接口调用 */
    public Map<String, Object> getGridDetail(String meshId) {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            // === 调用网格详情接口 ===
            HttpHeaders meshHeaders = buildHeaders("");
            String meshUrl = baseUrl + "/metrics/meshInfo/detail/" + meshId;
            ResponseEntity<String> meshResp = restTemplate.exchange(meshUrl, HttpMethod.GET, new HttpEntity<>(meshHeaders), String.class);
            JSONObject meshJson = new JSONObject(meshResp.getBody());
            JSONObject meshData = meshJson.optJSONObject("data");

            // 元信息
            Map<String, Object> meta = new LinkedHashMap<>();
            if (meshData != null) {
                meta.put("网格名称", meshData.optString("meshName", "未知"));
                meta.put("地址", meshData.optString("address", "未知"));
                meta.put("面积", meshData.optString("meshArea", "未知") + "㎡");
            }

            // === 从 resources 获取设备 ===
            List<Map<String, String>> devices = new ArrayList<>();
            JSONArray resources = meshData.optJSONArray("resources");
            if (resources != null) {
                for (int i = 0; i < resources.length(); i++) {
                    JSONObject r = resources.getJSONObject(i);
                    Map<String, String> dev = new HashMap<>();
                    dev.put("name", r.optString("name", "未知设备"));
                    dev.put("info", r.optString("categoryName", "未知类型"));
                    devices.add(dev);
                }
            }

            // === 调用设备接口 ===
            String queryString = "pageNum=1pageSize=10";
            HttpHeaders devHeaders = buildHeaders(queryString);
            String devUrl = baseUrl + "/device/dev/list/query?pageNum=1&pageSize=10";
            JSONObject payload = new JSONObject();
            payload.put("prodIds", new JSONArray().put("p_vrv"));
            payload.put("projectId", "1001");

            HttpEntity<String> devEntity = new HttpEntity<>(payload.toString(), devHeaders);
            ResponseEntity<String> devResp = restTemplate.exchange(devUrl, HttpMethod.POST, devEntity, String.class);
            JSONObject devJson = new JSONObject(devResp.getBody());
            JSONArray devDatas = devJson.optJSONObject("data") != null
                    ? devJson.getJSONObject("data").optJSONArray("datas")
                    : new JSONArray();

            // 添加设备接口的结果
            for (int i = 0; i < devDatas.length(); i++) {
                JSONObject item = devDatas.getJSONObject(i);
                Map<String, String> dev = new HashMap<>();
                dev.put("name", item.optString("name", "未知设备"));
                dev.put("info", item.optString("prodName", "未知类型"));
                devices.add(dev);
            }

            // === 组装结果 ===
            result.put("id", meshId);
            result.put("meta", meta);
            result.put("devices", devices);
            result.put("events", Collections.emptyList());
            result.put("services", Collections.emptyList());

        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return result;
    }
}