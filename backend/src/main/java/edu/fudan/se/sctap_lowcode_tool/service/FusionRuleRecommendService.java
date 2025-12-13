package edu.fudan.se.sctap_lowcode_tool.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.fudan.se.sctap_lowcode_tool.model.*;
import edu.fudan.se.sctap_lowcode_tool.repository.*;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.MilvusUtil;
import io.milvus.v2.service.vector.response.SearchResp;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FusionRuleRecommendService {

    @Resource
    private MilvusUtil milvusUtil;

    @Resource
    private DeviceRepository deviceRepository;

    @Resource
    private SpaceRepository spaceRepository;

    @Resource
    private FusionRuleRepository fusionRuleRepository;

    @Resource
    private FusionRuleBranchRepository fusionRuleBranchRepository;

    /* ===================== 输出结构（Controller 可直接返回） ===================== */

    public static class RagRecord {
        private final String pk;
        private final String objectType;
        private final String sourceTable;
        private final String sourceId;
        private final String description;
        private final float score;

        public RagRecord(String pk, String objectType, String sourceTable,
                         String sourceId, String description, float score) {
            this.pk = pk;
            this.objectType = objectType;
            this.sourceTable = sourceTable;
            this.sourceId = sourceId;
            this.description = description;
            this.score = score;
        }

        public String getPk() {
            return pk;
        }

        public String getObjectType() {
            return objectType;
        }

        public String getSourceTable() {
            return sourceTable;
        }

        public String getSourceId() {
            return sourceId;
        }

        public String getDescription() {
            return description;
        }

        public float getScore() {
            return score;
        }
    }

    /**
     * showQ=true 时返回该结构；showQ=false 时 Controller 直接返回 results 即可
     */
    public static class RuleRecommendResponse {
        public final String q;
        public final List<RagRecord> results;

        public RuleRecommendResponse(String q, List<RagRecord> results) {
            this.q = q;
            this.results = results;
        }
    }

    /* ===================== Milvus 基础 ===================== */

    private void ensureMilvusReady() {
        milvusUtil.ensureCollection();
    }

    /* ===================== 启动同步（全量写入 Milvus） ===================== */

    @Transactional(readOnly = true, transactionManager = "jpaTransactionManager")
    public void syncAllToMilvus() {
        ensureMilvusReady();

        for (DeviceInfo d : deviceRepository.findAll()) upsertDevice(d);
        for (SpaceInfo s : spaceRepository.findAll()) upsertSpace(s);
        for (FusionRule r : fusionRuleRepository.findAll()) upsertRule(r);
    }

    /* ===================== 描述生成（写入 Milvus 用） ===================== */

    public String buildDeviceDescription(DeviceInfo d) {
        String deviceName = Optional.ofNullable(d.getDeviceName()).orElse("未知设备");
        String deviceType = (d.getDeviceType() != null)
                ? Optional.ofNullable(d.getDeviceType().getDeviceTypeName()).orElse("未知类型")
                : "未知类型";
        String spaceName = (d.getSpace() != null)
                ? Optional.ofNullable(d.getSpace().getSpaceName()).orElse("未知空间")
                : "未知空间";

        Set<String> abilities = new LinkedHashSet<>();
        if (d.getActuatingFunctions() != null) {
            for (ActuatingFunctionDevice afd : d.getActuatingFunctions()) {
                ActuatingFunctionInfo f = afd.getActuatingFunction();
                if (f == null) continue;
                if (f.getDescription() != null && !f.getDescription().isBlank()) {
                    abilities.add(f.getDescription().trim());
                } else if (f.getName() != null && !f.getName().isBlank()) {
                    abilities.add(f.getName().trim());
                }
            }
        }

        String abilityText = abilities.isEmpty() ? "无显式能力" : String.join("、", abilities);
        String fixedProps = Optional.ofNullable(d.getFixedProperties()).orElse("无固定属性");

        return "设备：" + deviceName +
                "；类型：" + deviceType +
                "；所属空间：" + spaceName +
                "；能力：" + abilityText +
                "；固定属性：" + fixedProps;
    }

    public String buildSpaceDescription(SpaceInfo s) {
        String spaceName = Optional.ofNullable(s.getSpaceName()).orElse("未知空间");
        String projectName = (s.getProjectInfo() != null && s.getProjectInfo().getProjectName() != null)
                ? s.getProjectInfo().getProjectName()
                : "未知项目";

        Set<DeviceInfo> devicesInSpace = Optional.ofNullable(s.getSpaceDevices()).orElse(Set.of());

        Set<String> deviceNames = devicesInSpace.stream()
                .map(DeviceInfo::getDeviceName)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> deviceTypes = devicesInSpace.stream()
                .map(DeviceInfo::getDeviceType)
                .filter(Objects::nonNull)
                .map(DeviceTypeInfo::getDeviceTypeName)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> abilities = new LinkedHashSet<>();
        for (DeviceInfo d : devicesInSpace) {
            if (d.getActuatingFunctions() == null) continue;
            for (ActuatingFunctionDevice afd : d.getActuatingFunctions()) {
                ActuatingFunctionInfo f = afd.getActuatingFunction();
                if (f == null) continue;
                if (f.getDescription() != null && !f.getDescription().isBlank()) {
                    abilities.add(f.getDescription().trim());
                } else if (f.getName() != null && !f.getName().isBlank()) {
                    abilities.add(f.getName().trim());
                }
            }
        }

        Set<String> adjacentNames = Optional.ofNullable(s.getAdjacentSpaces()).orElse(Set.of())
                .stream()
                .map(SpaceInfo::getSpaceName)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        String devicesText = deviceNames.isEmpty() ? "暂无设备" : String.join("、", deviceNames);
        String typesText = deviceTypes.isEmpty() ? "未知类型" : String.join("、", deviceTypes);
        String abilityText = abilities.isEmpty() ? "无显式能力" : String.join("、", abilities);
        String adjacentText = adjacentNames.isEmpty() ? "无邻接空间" : String.join("、", adjacentNames);

        return "空间：" + spaceName +
                "；所属项目：" + projectName +
                "；包含设备：" + devicesText +
                "；设备类型：" + typesText +
                "；整体能力：" + abilityText +
                "；邻接空间：" + adjacentText;
    }

    public String buildRuleDescription(FusionRule rule, List<FusionRuleBranch> branches) {
        String name = Optional.ofNullable(rule.getRuleName()).orElse("未命名规则");
        int ruleId = rule.getRuleId();

        if (branches == null || branches.isEmpty()) {
            return "事件融合规则：" + name + "（ID=" + ruleId + "），当前暂无分支配置。";
        }

        List<String> branchTexts = new ArrayList<>();
        for (FusionRuleBranch b : branches) {
            String branchName = Optional.ofNullable(b.getBranchName()).orElse("未命名分支");
            String spaceName = (b.getSpace() != null && b.getSpace().getSpaceName() != null)
                    ? b.getSpace().getSpaceName()
                    : "未指定空间";
            String fusionTarget = Optional.ofNullable(b.getFusionTarget()).orElse("未指定融合目标");
            String status = Optional.ofNullable(b.getStatus()).orElse("未知状态");

            branchTexts.add("分支：" + branchName +
                    "；适用空间：" + spaceName +
                    "；融合目标：" + fusionTarget +
                    "；状态：" + status);
        }

        return "事件融合规则：" + name + "（ID=" + ruleId + "）。" + String.join("。", branchTexts);
    }

    /* ===================== Upsert（写入 Milvus） ===================== */

    public void upsertDevice(DeviceInfo d) {
        ensureMilvusReady();
        String sourceId = String.valueOf(d.getId());
        String pk = "device:" + sourceId;

        milvusUtil.deleteByPk(pk);

        String desc = buildDeviceDescription(d);
        float[] vec = milvusUtil.embed(desc);

        milvusUtil.insert(pk, "device", "devices", sourceId, desc, vec);
    }

    public void upsertSpace(SpaceInfo s) {
        ensureMilvusReady();
        String sourceId = String.valueOf(s.getSpaceId());
        String pk = "space:" + sourceId;

        milvusUtil.deleteByPk(pk);

        String desc = buildSpaceDescription(s);
        float[] vec = milvusUtil.embed(desc);

        milvusUtil.insert(pk, "space", "spaces", sourceId, desc, vec);
    }

    public void upsertRule(FusionRule rule) {
        ensureMilvusReady();
        String sourceId = String.valueOf(rule.getRuleId());
        String pk = "rule:" + sourceId;

        List<FusionRuleBranch> branches = fusionRuleBranchRepository.findByRule(rule);

        milvusUtil.deleteByPk(pk);

        String desc = buildRuleDescription(rule, branches);
        float[] vec = milvusUtil.embed(desc);

        milvusUtil.insert(pk, "rule", "fusion_rule", sourceId, desc, vec);
    }

    public void upsertRuleByBranch(FusionRuleBranch branch) {
        FusionRule rule = branch.getRule();
        if (rule == null) {
            throw new IllegalArgumentException("FusionRuleBranch 未关联 FusionRule，无法更新向量库");
        }
        upsertRule(rule);
    }

    public void deleteRuleVector(int ruleId) {
        milvusUtil.deleteByPk("rule:" + ruleId);
    }

    /**
     * 上传/更新分支后：只负责把对应主干写入/更新到 Milvus。
     * 推荐榜不在这里做（推荐榜由 getLatestRuleRecommendations 实时从 DB 计算）。
     */
    public void onBranchRuleUploaded(FusionRuleBranch branch) {
        upsertRuleByBranch(branch);
    }

    /* ===================== Milvus 检索：规则 ===================== */

    public List<RagRecord> searchRules(String query, int topK) {
        ensureMilvusReady();

        float[] qv = milvusUtil.embed(query);

        SearchResp resp = milvusUtil.search(
                qv,
                topK,
                "object_type == \"rule\"",
                Arrays.asList("pk", "object_type", "source_table", "source_id", "description")
        );

        return toRagRecords(resp);
    }

    private List<RagRecord> toRagRecords(SearchResp resp) {
        List<RagRecord> out = new ArrayList<>();
        for (List<SearchResp.SearchResult> batch : resp.getSearchResults()) {
            for (SearchResp.SearchResult r : batch) {
                Map<String, Object> e = r.getEntity();
                out.add(new RagRecord(
                        (String) e.get("pk"),
                        (String) e.get("object_type"),
                        (String) e.get("source_table"),
                        (String) e.get("source_id"),
                        (String) e.get("description"),
                        r.getScore()
                ));
            }
        }
        return out;
    }

    /* ===================== 核心：从 DB 最新 branch 拼成一个 q 一次检索 ===================== */

    /**
     * showQ=true：返回 (q + results)
     * 逻辑：
     * 1) 从 DB 按 branchId DESC 取最新 seedN 条分支
     * 2) 把它们的 ruleJson 拼成一个大 q（去掉多余空白）
     * 3) q 发到 Milvus 做一次 rule search，返回 topK=k
     */
    public RuleRecommendResponse getLatestRuleRecommendationsWithQ(int seedN, int k, boolean showQ) {
        ensureMilvusReady();

        int n = Math.max(1, seedN);
        int topK = Math.max(1, k);

        List<String> seeds = fetchLatestBranchRuleJsonSeeds(n);
        if (seeds.isEmpty()) {
            return new RuleRecommendResponse("", List.of());
        }

        // 1) 先从所有 seeds 中递归抽取 deviceIds
        Set<Integer> deviceIds = new LinkedHashSet<>();
        for (String ruleJson : seeds) {
            deviceIds.addAll(extractDeviceIdsFromRuleJson(ruleJson));
        }

        // 2) 批量查 DB：id -> name
        Map<Integer, String> idToName = new HashMap<>();
        if (!deviceIds.isEmpty()) {
            Iterable<DeviceInfo> devices = deviceRepository.findAllById(deviceIds);
            for (DeviceInfo d : devices) {
                if (d == null || d.getId() == null) continue;
                idToName.put(d.getId(), Optional.ofNullable(d.getDeviceName()).orElse(""));
            }
        }

        // 3) 每条 ruleJson -> 语义 q（不含“节点类型”，包含“设备=id(name)”）
        //    同时去重，避免 q 里重复块
        LinkedHashSet<String> seedQs = new LinkedHashSet<>();
        for (String rj : seeds) {
            String s = normalizeSpaces(buildSemanticQFromRuleJson(rj, idToName));
            if (!s.isBlank()) seedQs.add(s);
        }

        if (seedQs.isEmpty()) {
            return new RuleRecommendResponse("", List.of());
        }

        // 4) 拼成一个大 q 一次发给 Milvus
        String q = String.join("。", seedQs);

        List<RagRecord> results;
        try {
            results = searchRules(q, topK);
        } catch (Exception ex) {
            results = List.of();
        }

        return showQ ? new RuleRecommendResponse(q, results)
                : new RuleRecommendResponse("", results);
    }

    /* ===================== q 语义化：JSON 递归抽取 ===================== */

    private static final com.fasterxml.jackson.databind.ObjectMapper Q_MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    // 允许抓取的 id 字段名（全部按“规范化key”匹配：去下划线/短横线并转小写）
    private static final Set<String> ID_KEYS = Set.of(
            "sensorid", "actuatorid", "deviceid", "id"
    );

    // 允许作为“容器字段”的名字：遇到这些字段（比如 sensor/device）会继续深入找 id
    private static final Set<String> CONTAINER_KEYS = Set.of(
            "sensor", "device", "actuator", "meta", "data", "info", "payload"
    );

    /**
     * 从 ruleJson 中抽取可能的设备ID：
     * - Sensor 节点：sensorId
     * - Actuator 节点：actuatorId / deviceId（按你 DSL 可能存在的字段）
     */
    private Set<Integer> extractDeviceIdsFromRuleJson(String ruleJson) {
        if (ruleJson == null || ruleJson.isBlank()) return Set.of();

        try {
            JsonNode root = Q_MAPPER.readTree(ruleJson);
            if (root == null || !root.isObject()) return Set.of();

            Set<Integer> ids = new LinkedHashSet<>();
            Iterator<Map.Entry<String, JsonNode>> it = root.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                String key = e.getKey();
                JsonNode node = e.getValue();

                if (key == null) continue;
                if ("steps".equalsIgnoreCase(key) || "rulename".equalsIgnoreCase(key)) continue;
                if (node == null) continue;

                collectIdsRecursively(node, ids, 0);
            }
            return ids;
        } catch (Exception ex) {
            return Set.of();
        }
    }

    private void collectIdsRecursively(JsonNode node, Set<Integer> out, int depth) {
        if (node == null || depth > 12) return; // 防御：避免极端深度

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                String rawKey = e.getKey();
                JsonNode v = e.getValue();
                if (rawKey == null) continue;

                String key = normalizeKey(rawKey);

                // 1) 命中 id key：尝试解析数值
                if (ID_KEYS.contains(key)) {
                    Integer id = parseIntFlexible(v);
                    if (id != null && id > 0) out.add(id);
                }

                // 2) 容器字段继续深入（或直接对所有对象/数组都深入也可以）
                if (v != null) {
                    boolean shouldDive =
                            v.isObject() || v.isArray() || CONTAINER_KEYS.contains(key);
                    if (shouldDive) {
                        collectIdsRecursively(v, out, depth + 1);
                    }
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                collectIdsRecursively(child, out, depth + 1);
            }
        }
    }

    private Integer parseIntFlexible(JsonNode v) {
        if (v == null || v.isNull()) return null;
        if (v.isInt() || v.isLong()) return v.asInt();
        if (v.isTextual()) {
            String t = v.asText().trim();
            if (t.isEmpty()) return null;
            // 只接受纯数字（避免把别的东西误判）
            if (t.matches("^\\d+$")) {
                try {
                    return Integer.parseInt(t);
                } catch (Exception ignore) {
                }
            }
        }
        return null;
    }

    private String normalizeKey(String k) {
        return k == null ? "" : k.replaceAll("[_\\-\\s]", "").toLowerCase(Locale.ROOT);
    }

    /**
     * ruleJson -> 语义 q（不含节点类型）
     * - 设备：从 JSON 递归抽 id，拼成 id(name)
     * - 位置：location
     * - 传感能力：sensingFunction
     * - 执行能力：function
     * - 操作符：operator
     * - 阈值存在性：value/threshold/min/max
     */
    private String buildSemanticQFromRuleJson(String ruleJson, Map<Integer, String> idToName) {
        if (ruleJson == null || ruleJson.isBlank()) return "";

        try {
            JsonNode root = Q_MAPPER.readTree(ruleJson);
            if (root == null || !root.isObject()) {
                return normalizeSpaces(ruleJson);
            }

            // 设备：从整条 ruleJson 抽（而不是只看单个 node）
            Set<Integer> ids = extractDeviceIdsFromRuleJson(ruleJson);
            Set<String> devices = new LinkedHashSet<>();
            for (Integer id : ids) {
                if (id == null || id <= 0) continue;
                String name = (idToName != null ? idToName.getOrDefault(id, "") : "");
                if (name != null && !name.isBlank()) devices.add(id + "(" + name.trim() + ")");
                else devices.add(String.valueOf(id));
            }

            Set<String> locations = new LinkedHashSet<>();
            Set<String> sensingFuncs = new LinkedHashSet<>();
            Set<String> actuatingFuncs = new LinkedHashSet<>();
            Set<String> operators = new LinkedHashSet<>();

            boolean hasThresholdLikeValue = false;

            Iterator<Map.Entry<String, JsonNode>> it = root.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                String key = e.getKey();
                JsonNode node = e.getValue();

                if (key == null) continue;
                if ("steps".equalsIgnoreCase(key) || "rulename".equalsIgnoreCase(key)) continue;
                if (node == null || !node.isObject()) continue;

                String loc = text(node, "location");
                if (!loc.isBlank()) locations.add(loc);

                String sensing = text(node, "sensingFunction");
                if (!sensing.isBlank()) sensingFuncs.add(sensing);

                String act = text(node, "function");
                if (!act.isBlank()) actuatingFuncs.add(act);

                String op = text(node, "operator");
                if (!op.isBlank()) operators.add(op);

                if (node.has("value") || node.has("threshold") || node.has("min") || node.has("max")) {
                    hasThresholdLikeValue = true;
                }
            }

            StringBuilder sb = new StringBuilder();
            appendSet(sb, "设备", devices);
            appendSet(sb, "位置", locations);
            appendSet(sb, "传感能力", sensingFuncs);
            appendSet(sb, "执行能力", actuatingFuncs);
            appendSet(sb, "操作符", operators);
            if (hasThresholdLikeValue) {
                if (!sb.isEmpty()) sb.append("；");
                sb.append("包含阈值=<num>");
            }

            String q = normalizeSpaces(sb.toString());
            // 如果抽不出任何特征，就退回原文（防止返回空）
            return q.isBlank() ? normalizeSpaces(ruleJson) : q;

        } catch (Exception ex) {
            // JSON 不合法/解析失败：退回原文
            return normalizeSpaces(ruleJson);
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return "";
        if (v.isTextual()) return v.asText().trim();
        return "";
    }

    private void appendSet(StringBuilder sb, String label, Set<String> set) {
        if (set == null || set.isEmpty()) return;
        if (!sb.isEmpty()) sb.append("；");
        sb.append(label).append("=");
        sb.append(String.join("、", set));
    }

    /* ===================== DB：取最新 seedN 条 branch.ruleJson ===================== */

    private List<String> fetchLatestBranchRuleJsonSeeds(int seedN) {
        var pageable = PageRequest.of(0, seedN, Sort.by(Sort.Direction.DESC, "branchId"));
        List<FusionRuleBranch> latest = fusionRuleBranchRepository.findAll(pageable).getContent();

        return latest.stream()
                .map(FusionRuleBranch::getRuleJson)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String normalizeSpaces(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+", " ").trim();
    }
}