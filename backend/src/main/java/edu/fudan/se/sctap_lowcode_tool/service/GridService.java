package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.*;
import edu.fudan.se.sctap_lowcode_tool.repository.GridMeshRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.SignUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

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

    @Resource
    private EnvEventService envEventService;

    @Resource
    private EnvServiceService envServiceService;

    @Resource
    private EnvPropertyService envPropertyService;

    @Resource
    private AppGridService appGridService;

    @Resource
    private GridMeshRepository gridMeshRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 根据 meshCode 查找数据库中对应的网格信息
     */
    private Map<String, Object> findGridInfo(String meshCode) {
        try {
            String sql = "SELECT id, mesh_no, mesh_name, mesh_nature, mesh_area FROM grid_list WHERE LOWER(mesh_no) = LOWER(?) LIMIT 1";
            Map<String, Object> record = jdbcTemplate.queryForMap(sql, meshCode);
            System.out.println("找到网格：" + record);
            return record;
        } catch (Exception e) {
            System.out.println("未找到网格 mesh_no=" + meshCode + "：" + e.getMessage());
            return null;
        }
    }

    /**
     * 生成签名头
     */
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

    /**
     * 获取网格详情
     */
    public Map<String, Object> getGridDetail(String meshCode) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            // 1. 从数据库获取基础信息
            Map<String, Object> grid = findGridInfo(meshCode);
            if (grid == null) {
                throw new RuntimeException("未找到对应网格: " + meshCode);
            }

            String meshId = (String) grid.get("id");
            String meshName = (String) grid.get("mesh_name");
            String meshType = (String) grid.get("mesh_nature");
            Object meshArea = grid.get("mesh_area");

            // 2. 构建 meta 元信息
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("网格编号", meshCode);
            meta.put("网格名称", meshName);
            meta.put("网格类型", meshType);
            meta.put("面积", meshArea != null ? meshArea + "㎡" : "未知");

            // 3️. 调远程接口 (若有 meshId)
            List<Map<String, String>> devices = new ArrayList<>();
            try {
                HttpHeaders headers = buildHeaders("");
                String meshUrl = baseUrl + "/metrics/meshInfo/detail/" + meshId;
                ResponseEntity<String> meshResp = restTemplate.exchange(meshUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);

                JSONObject meshJson = new JSONObject(meshResp.getBody());
                JSONObject meshData = meshJson.optJSONObject("data");
                if (meshData != null && meshData.has("resources")) {
                    JSONArray resources = meshData.optJSONArray("resources");
                    if (resources != null) {
                        for (int i = 0; i < resources.length(); i++) {
                            JSONObject r = resources.getJSONObject(i);
                            Map<String, String> dev = new LinkedHashMap<>();
                            dev.put("name", r.optString("name", "未知设备"));
                            dev.put("info", r.optString("categoryName", "未知类型"));
                            devices.add(dev);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ 调用远程接口失败，继续使用本地数据: " + e.getMessage());
            }

            // 4️. 统一输出格式
            result.put("id", meshId);
            result.put("meta", meta);
            result.put("devices", devices);

            // 5️. 获取环境级事件列表
            List<EnvEvent> envEvents = envEventService.findByGridId(meshId);
            result.put("events", envEvents);

            // 6. 获取环境级服务列表
            List<EnvService> envServices = envServiceService.findByGridId(meshId);
            result.put("services", envServices);

            // 7. 获取环境级属性列表
            List<EnvProperty> envProperties = envPropertyService.findByGridId(meshId);
            result.put("properties", envProperties);

            // 8. 获取应用级信息
            List<AppRuleInfo> appRules = appGridService.findByGridId(meshId);
            result.put("applications", appRules);

        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 获取网格信息
     * */
    public GridMesh getGridById(String gridId) {
        return gridMeshRepository.findById(gridId).orElse(null);
    }

    /**
     * 根据类型获取网格列表
     * */
    public List<GridMesh> getGridListByType(String gridId) {
        // 获取网格信息
        GridMesh gridMesh = getGridById(gridId);
        if(gridMesh==null) {
            return null;
        }
        String meshNature = gridMesh.getMeshNature();
        String meshType = gridMesh.getMeshType();
        return gridMeshRepository.findByMeshNatureAndMeshType(meshNature, meshType);
    }
}