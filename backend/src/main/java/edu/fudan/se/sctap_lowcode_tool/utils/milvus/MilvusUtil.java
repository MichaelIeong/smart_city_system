package edu.fudan.se.sctap_lowcode_tool.utils.milvus;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.fudan.se.sctap_lowcode_tool.model.*;
import edu.fudan.se.sctap_lowcode_tool.repository.DeviceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.SpaceRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.repository.FusionRuleBranchRepository;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MilvusUtil {

    private static final String COLLECTION_NAME = "fusion_rag_collection";
    private static final int VECTOR_DIMENSION = 1024;

    private final MilvusClientV2 milvusClient;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();

    @Value("${ollama.host}")
    private String ollamaHost;

    @Value("${ollama.port}")
    private int ollamaPort;

    @Value("${ollama.model}")
    private String ollamaModel;

    @Resource
    private DeviceRepository deviceRepository;

    @Resource
    private SpaceRepository spaceRepository;

    @Resource
    private FusionRuleRepository fusionRuleRepository;

    @Resource
    private FusionRuleBranchRepository fusionRuleBranchRepository;

    public MilvusUtil(MilvusClientV2 milvusClient) {
        this.milvusClient = milvusClient;
    }

    /* ===================== 嵌入模型调用 ===================== */

    /**
     * 调用远端 Ollama embeddings，将文本编码为向量。
     */
    private float[] embed(String text) {
        String url = "http://" + ollamaHost + ":" + ollamaPort + "/api/embeddings";

        Map<String, Object> body = new HashMap<>();
        body.put("model", ollamaModel);
        body.put("prompt", text);

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = restTemplate.postForObject(url, body, Map.class);
        if (resp == null) {
            throw new IllegalStateException("调用 Ollama embeddings 返回 null");
        }

        Object embObj = resp.get("embedding");
        if (embObj == null) {
            embObj = resp.get("embeddings");
        }
        if (!(embObj instanceof List<?> list)) {
            throw new IllegalStateException("Ollama embeddings 返回格式异常: " + resp);
        }

        float[] vec = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object v = list.get(i);
            if (v instanceof Number num) {
                vec[i] = num.floatValue();
            } else {
                throw new IllegalStateException("embedding 元素不是数字: " + v);
            }
        }

        if (vec.length != VECTOR_DIMENSION) {
            throw new IllegalStateException("向量维度不匹配：得到 " + vec.length + "，预期 " + VECTOR_DIMENSION);
        }

        return vec;
    }

    /* ===================== Collection 管理 ===================== */

    /**
     * 如果向量集合不存在，则创建统一的 RAG Collection。
     */
    private void createCollectionIfNotExists() {
        boolean exists = milvusClient.hasCollection(
                HasCollectionReq.builder().collectionName(COLLECTION_NAME).build()
        );
        if (exists) return;

        CreateCollectionReq.CollectionSchema schema = milvusClient.createSchema();

        schema.addField(AddFieldReq.builder()
                .fieldName("pk")
                .dataType(DataType.VarChar)
                .isPrimaryKey(true)
                .autoID(false)
                .maxLength(128)
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("object_type")  // device / space / rule
                .dataType(DataType.VarChar)
                .maxLength(32)
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("source_table")
                .dataType(DataType.VarChar)
                .maxLength(64)
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("source_id")
                .dataType(DataType.VarChar)
                .maxLength(64)
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("description")
                .dataType(DataType.VarChar)
                .maxLength(10000)
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("description_vector")
                .dataType(DataType.FloatVector)
                .dimension(VECTOR_DIMENSION)
                .build());

        IndexParam indexParam = IndexParam.builder()
                .fieldName("description_vector")
                .metricType(IndexParam.MetricType.COSINE)
                .build();

        CreateCollectionReq req = CreateCollectionReq.builder()
                .collectionName(COLLECTION_NAME)
                .collectionSchema(schema)
                .indexParams(Collections.singletonList(indexParam))
                .build();

        milvusClient.createCollection(req);
    }

    /**
     * 向统一 RAG Collection 中写入一条记录。
     */
    private void insertRagRecord(String objectType,
                                 String sourceTable,
                                 String sourceId,
                                 String description) {

        createCollectionIfNotExists();
        float[] vec = embed(description);

        JsonObject json = new JsonObject();
        String pk = objectType + ":" + sourceId;

        json.addProperty("pk", pk);
        json.addProperty("object_type", objectType);
        json.addProperty("source_table", sourceTable);
        json.addProperty("source_id", sourceId);
        json.addProperty("description", description);
        json.add("description_vector", gson.toJsonTree(vec));

        InsertReq insertReq = InsertReq.builder()
                .collectionName(COLLECTION_NAME)
                .data(Collections.singletonList(json))
                .build();

        InsertResp resp = milvusClient.insert(insertReq);
    }

    /**
     * 按 object_type + sourceId 删除一条记录。
     */
    public void deleteByObject(String objectType, String sourceId) {
        createCollectionIfNotExists();
        String pk = objectType + ":" + sourceId;
        DeleteReq deleteReq = DeleteReq.builder()
                .collectionName(COLLECTION_NAME)
                .ids(Collections.singletonList(pk))
                .build();
        milvusClient.delete(deleteReq);
    }

    /* ===================== 标准化：设备 / 空间 / 规则 ===================== */

    /**
     * 将一条设备记录标准化为面向语义检索的中文描述。
     */
    private String buildDeviceDescription(DeviceInfo d) {
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
                if (f != null) {
                    if (f.getDescription() != null && !f.getDescription().isEmpty()) {
                        abilities.add(f.getDescription());
                    } else if (f.getName() != null) {
                        abilities.add(f.getName());
                    }
                }
            }
        }
        String abilityText = abilities.isEmpty()
                ? "无显式能力"
                : String.join("、", abilities);

        String fixedProps = Optional.ofNullable(d.getFixedProperties())
                .orElse("无固定属性");

        return "设备：" + deviceName +
                "；类型：" + deviceType +
                "；所属空间：" + spaceName +
                "；能力：" + abilityText +
                "；固定属性：" + fixedProps;
    }

    /**
     * 将一个空间及其下设备标准化为空间语义描述。
     */
    private String buildSpaceDescription(SpaceInfo s) {
        String spaceName = Optional.ofNullable(s.getSpaceName()).orElse("未知空间");
        String projectName = (s.getProjectInfo() != null && s.getProjectInfo().getProjectName() != null)
                ? s.getProjectInfo().getProjectName()
                : "未知项目";

        Set<DeviceInfo> devicesInSpace = s.getSpaceDevices();

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
            if (d.getActuatingFunctions() != null) {
                for (ActuatingFunctionDevice afd : d.getActuatingFunctions()) {
                    ActuatingFunctionInfo f = afd.getActuatingFunction();
                    if (f != null) {
                        if (f.getDescription() != null && !f.getDescription().isEmpty()) {
                            abilities.add(f.getDescription());
                        } else if (f.getName() != null) {
                            abilities.add(f.getName());
                        }
                    }
                }
            }
        }

        Set<String> adjacentNames = s.getAdjacentSpaces().stream()
                .map(SpaceInfo::getSpaceName)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        String devicesText = deviceNames.isEmpty()
                ? "暂无设备"
                : String.join("、", deviceNames);
        String typesText = deviceTypes.isEmpty()
                ? "未知类型"
                : String.join("、", deviceTypes);
        String abilityText = abilities.isEmpty()
                ? "无显式能力"
                : String.join("、", abilities);
        String adjacentText = adjacentNames.isEmpty()
                ? "无邻接空间"
                : String.join("、", adjacentNames);

        return "空间：" + spaceName +
                "；所属项目：" + projectName +
                "；包含设备：" + devicesText +
                "；设备类型：" + typesText +
                "；整体能力：" + abilityText +
                "；邻接空间：" + adjacentText;
    }

    /**
     * 将主规则及所有分支标准化为规则语义描述，用于相似规则和规则迁移检索。
     */
    private String buildRuleDescription(FusionRule rule, List<FusionRuleBranch> branches) {
        String name = Optional.ofNullable(rule.getRuleName()).orElse("未命名规则");
        int ruleId = rule.getRuleId();

        if (branches == null || branches.isEmpty()) {
            return "事件融合规则：" + name +
                    "（ID=" + ruleId + "），当前暂无分支配置。";
        }

        List<String> branchTexts = new ArrayList<>();
        for (FusionRuleBranch b : branches) {
            String branchName = Optional.ofNullable(b.getBranchName()).orElse("未命名分支");
            String spaceName = (b.getSpace() != null && b.getSpace().getSpaceName() != null)
                    ? b.getSpace().getSpaceName()
                    : "未指定空间";
            String fusionTarget = Optional.ofNullable(b.getFusionTarget()).orElse("未指定融合目标");
            String status = Optional.ofNullable(b.getStatus()).orElse("未知状态");

            String dslInfo = "";
            if (b.getRuleJson() != null && !b.getRuleJson().isEmpty()) {
                dslInfo += "包含规则DSL定义";
            }
            if (b.getFlowJson() != null && !b.getFlowJson().isEmpty()) {
                if (!dslInfo.isEmpty()) dslInfo += "，";
                dslInfo += "包含节点流定义";
            }
            if (dslInfo.isEmpty()) {
                dslInfo = "尚未配置详细DSL";
            }

            String text = "分支：" + branchName +
                    "；适用空间：" + spaceName +
                    "；融合目标：" + fusionTarget +
                    "；状态：" + status +
                    "；DSL：" + dslInfo;
            branchTexts.add(text);
        }

        return "事件融合规则：" + name +
                "（ID=" + ruleId + "）。" +
                String.join("。", branchTexts);
    }

    /* ===================== 单条 upsert 接口 ===================== */

    /**
     * 设备数据变更后，更新对应的向量条目。
     */
    public void upsertDevice(DeviceInfo d) {
        createCollectionIfNotExists();
        String sourceId = d.getId().toString();
        deleteByObject("device", sourceId);
        String desc = buildDeviceDescription(d);
        insertRagRecord("device", "devices", sourceId, desc);
    }

    /**
     * 空间数据变更后，更新对应的向量条目。
     */
    public void upsertSpace(SpaceInfo s) {
        createCollectionIfNotExists();
        String sourceId = s.getSpaceId().toString();
        deleteByObject("space", sourceId);
        String desc = buildSpaceDescription(s);
        insertRagRecord("space", "spaces", sourceId, desc);
    }

    /**
     * 主规则或其分支整体变化后，更新对应规则向量条目。
     */
    public void upsertRule(FusionRule rule) {
        createCollectionIfNotExists();
        String sourceId = String.valueOf(rule.getRuleId());
        List<FusionRuleBranch> branches = fusionRuleBranchRepository.findByRule(rule);
        deleteByObject("rule", sourceId);
        String desc = buildRuleDescription(rule, branches);
        insertRagRecord("rule", "fusion_rule", sourceId, desc);
    }

    /**
     * 某个分支变化时，通过分支所属规则更新整条规则的语义向量。
     */
    public void upsertRuleByBranch(FusionRuleBranch branch) {
        FusionRule rule = branch.getRule();
        if (rule == null) {
            throw new IllegalArgumentException("FusionRuleBranch 未关联 FusionRule，无法更新向量库");
        }
        upsertRule(rule);
    }

    /* ===================== 启动时全量同步 ===================== */

    /**
     * 全量同步 devices 表到 Milvus。
     */
    @Transactional(readOnly = true, transactionManager = "jpaTransactionManager")
    public void syncDevicesToMilvus() {
        createCollectionIfNotExists();
        List<DeviceInfo> devices = deviceRepository.findAll();
        for (DeviceInfo d : devices) {
            upsertDevice(d);
        }
    }

    /**
     * 全量同步 spaces 表到 Milvus。
     */
    @Transactional(readOnly = true, transactionManager = "jpaTransactionManager")
    public void syncSpacesToMilvus() {
        createCollectionIfNotExists();
        List<SpaceInfo> spaces = spaceRepository.findAll();
        for (SpaceInfo s : spaces) {
            upsertSpace(s);
        }
    }

    /**
     * 全量同步融合规则（主规则 + 分支）到 Milvus。
     */
    @Transactional(readOnly = true, transactionManager = "jpaTransactionManager")
    public void syncRulesToMilvus() {
        createCollectionIfNotExists();
        List<FusionRule> rules = fusionRuleRepository.findAll();
        for (FusionRule r : rules) {
            upsertRule(r);
        }
    }

    /* ===================== RAG 检索接口 ===================== */

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
     * 通用 RAG 检索接口：返回相似设备 / 空间 / 规则的标准化描述。
     *
     * @param query 自然语言查询或规则文本
     * @param topK  返回数量
     */
    public List<RagRecord> searchRules(String query, int topK) {
        createCollectionIfNotExists();

        float[] queryVec = embed(query);

        SearchReq req = SearchReq.builder()
                .collectionName(COLLECTION_NAME)
                .data(Collections.singletonList(new FloatVec(queryVec)))
                .topK(topK)
                .metricType(IndexParam.MetricType.COSINE)
                // 核心：类型过滤下推到 Milvus
                .filter("object_type == \"rule\"")
                .outputFields(Arrays.asList(
                        "pk",
                        "source_id",
                        "description"
                ))
                .build();

        SearchResp resp = milvusClient.search(req);

        List<RagRecord> results = new ArrayList<>();
        for (List<SearchResp.SearchResult> batch : resp.getSearchResults()) {
            for (SearchResp.SearchResult r : batch) {
                Map<String, Object> e = r.getEntity();
                results.add(new RagRecord(
                        (String) e.get("pk"),
                        "rule",
                        "fusion_rule",
                        (String) e.get("source_id"),
                        (String) e.get("description"),
                        r.getScore()
                ));
            }
        }
        return results;
    }
}