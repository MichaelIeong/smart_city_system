package edu.fudan.se.sctap_lowcode_tool.service;
import edu.fudan.se.sctap_lowcode_tool.utils.SignUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;


@Service
public class GridService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${tsl.app.base-url}")
    private String baseUrl;

    @Value("${tsl.app.id}")
    private String appId;

    @Value("${tsl.app.code}")
    private String appCode;

    @Value("${tsl.app.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    private String findRealMeshId(String meshCode) {
        try {
            int num = Integer.parseInt(meshCode);
            String meshNo = "f-city-" + num;
            System.out.println("尝试查询 mesh_no = '" + meshNo + "'");

            String sql = "SELECT id FROM grid_list WHERE LOWER(mesh_no) = LOWER(?) LIMIT 1";
            String realId = jdbcTemplate.queryForObject(sql, String.class, meshNo);

            System.out.println("找到对应: " + meshCode + " → " + meshNo + " → " + realId);
            return realId;
        } catch (Exception e) {
            System.out.println("无法找到 meshCode 对应的真实网格ID: " + meshCode + " (" + e.getMessage() + ")");
            return null;
        }
    }

    /** 生成签名头 */
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

    /** 查询网格详情 */
    public Map<String, Object> getGridDetail(String meshCode) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            //mesh_code → mesh_no → 真实 ID
            String meshId = findRealMeshId(meshCode);
            if (meshId == null) {
                throw new RuntimeException("未找到对应的网格ID: " + meshCode);
            }

            //调远程接口
            HttpHeaders headers = buildHeaders("");
            String meshUrl = baseUrl + "/metrics/meshInfo/detail/" + meshId;
            ResponseEntity<String> meshResp = restTemplate.exchange(meshUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            JSONObject meshJson = new JSONObject(meshResp.getBody());
            JSONObject meshData = meshJson.optJSONObject("data");

            //解析数据
            Map<String, Object> meta = new LinkedHashMap<>();
            if (meshData != null) {
                meta.put("网格名称", meshData.optString("meshName", "未知"));
                meta.put("地址", meshData.optString("address", "未知"));
                meta.put("面积", meshData.optString("meshArea", "未知") + "㎡");
            }

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

            //返回统一格式
            result.put("id", meshId);
            result.put("meta", meta);
            result.put("devices", devices);
            result.put("events", Collections.emptyList());
            result.put("services", Collections.emptyList());

        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}