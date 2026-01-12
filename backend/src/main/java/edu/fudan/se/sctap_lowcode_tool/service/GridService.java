package edu.fudan.se.sctap_lowcode_tool.service;

import edu.fudan.se.sctap_lowcode_tool.model.*;
// 移除不必要的导入，如 SignUtil, Http*, RestTemplate
import edu.fudan.se.sctap_lowcode_tool.repository.GridMeshRepository;
import jakarta.annotation.Resource;
// 移除 @Value
import org.springframework.stereotype.Service;
// 移除 org.json.* 依赖
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

@Service
public class GridService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 移除 @Value 外部接口配置
    // 移除 EnvEventService, EnvServiceService, EnvPropertyService, AppGridService 的注入
    // 假设这些 Service 已经正确地从本地 DB 获取数据

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


    // 移除 RestTemplate

    /**
     * 根据 meshCode 查找数据库中对应的网格信息 (保留)
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
     * 获取系统中所有网格列表（全局资源，不区分项目）
     */
    public List<Map<String, Object>> getAllGridList() {
        try {
            // 直接查询 grid_list 表中所有的网格编号和名称
            String sql = "SELECT id, mesh_no, mesh_name FROM grid_list";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            System.err.println("获取全量网格列表失败：" + e.getMessage());
            return new ArrayList<>();
        }
    }
    /**
     * 【新增】：根据网格ID (meshId) 从本地 tsl_devices 表中获取设备列表
     */
    private List<Map<String, String>> fetchLocalDevices(String meshId) {
        try {
            // 关键修改：JOIN tsl_product table
            String sql = "SELECT d.device_name, p.product_instruction " +
                    "FROM tsl_devices d " +
                    "LEFT JOIN tsl_product p ON d.product_id = p.product_id " +
                    "WHERE d.mesh_id = ?";

            return jdbcTemplate.query(sql, new Object[]{meshId}, (rs, rowNum) -> {
                Map<String, String> dev = new LinkedHashMap<>();
                String instruction = rs.getString("product_instruction");
                String productOps = "无操作指令";

                // 格式化产品指令逻辑 (与TslDeviceService中保持一致)
                if (instruction != null && instruction.startsWith("[")) {
                    productOps = instruction
                            .replace("[", "")
                            .replace("]", "")
                            .replace("\"", "")
                            .replace(",", "，");
                } else if (instruction != null) {
                    productOps = instruction;
                }

                dev.put("name", rs.getString("device_name"));
                // 将格式化后的指令映射到前端需要的 'info' 字段
                dev.put("info", productOps);
                return dev;
            });
        } catch (Exception e) {
            System.err.println("获取本地设备列表失败 (meshId=" + meshId + ")：" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取网格详情 (重写远程调用部分)
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

            // 2. 构建 meta 元信息 (保留)
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("网格编号", meshCode);
            meta.put("网格名称", meshName);
            meta.put("网格类型", meshType);
            meta.put("面积", meshArea != null ? meshArea + "㎡" : "未知");

            // 3️. 替换远程接口调用：从本地 tsl_devices 表获取设备
            List<Map<String, String>> devices = fetchLocalDevices(meshId);
            System.out.println("✅ 从本地数据库获取到 " + devices.size() + " 个设备。");

            // 4️. 统一输出格式 (保留)
            result.put("id", meshId);
            result.put("meta", meta);
            result.put("devices", devices);

            // 5️. 获取环境级事件列表 (保留，依赖其他 Service)
            List<EnvEvent> envEvents = envEventService.findByGridId(meshId);
            result.put("events", envEvents);

            // 6. 获取环境级服务列表 (保留，依赖其他 Service)
            List<EnvService> envServices = envServiceService.findByGridId(meshId);
            result.put("services", envServices);

            // 7. 获取环境级属性列表 (保留，依赖其他 Service)
            List<EnvProperty> envProperties = envPropertyService.findByGridId(meshId);
            result.put("properties", envProperties);

            // 8. 获取应用级信息 (保留，依赖其他 Service)
            List<AppRuleInfo> appRules = appGridService.findByGridId(meshId);
            result.put("applications", appRules);

        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
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