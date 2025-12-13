package edu.fudan.se.sctap_lowcode_tool.utils.milvus;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

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

    public MilvusUtil(MilvusClientV2 milvusClient) {
        this.milvusClient = milvusClient;
    }

    public String collectionName() {
        return COLLECTION_NAME;
    }

    public int vectorDimension() {
        return VECTOR_DIMENSION;
    }

    /**
     * 调用远端 Ollama embeddings，将文本编码为向量。
     * 约束：返回维度必须等于 VECTOR_DIMENSION，否则直接抛错，避免写入脏数据。
     */
    public float[] embed(String text) {
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
        if (embObj == null) embObj = resp.get("embeddings");

        if (!(embObj instanceof List<?> list)) {
            throw new IllegalStateException("Ollama embeddings 返回格式异常: " + resp);
        }

        float[] vec = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object v = list.get(i);
            if (!(v instanceof Number num)) {
                throw new IllegalStateException("embedding 元素不是数字: " + v);
            }
            vec[i] = num.floatValue();
        }

        if (vec.length != VECTOR_DIMENSION) {
            throw new IllegalStateException("向量维度不匹配：得到 " + vec.length + "，预期 " + VECTOR_DIMENSION);
        }
        return vec;
    }

    /**
     * 确保 Collection 存在（不存在则创建）。仅定义一次 schema 与 index。
     */
    public void ensureCollection() {
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
     * 以 pk 为主键写入一条记录（上层负责先 delete 再 insert 或直接用幂等策略）。
     */
    public void insert(String pk,
                       String objectType,
                       String sourceTable,
                       String sourceId,
                       String description,
                       float[] vector) {
        ensureCollection();

        JsonObject json = new JsonObject();
        json.addProperty("pk", pk);
        json.addProperty("object_type", objectType);
        json.addProperty("source_table", sourceTable);
        json.addProperty("source_id", sourceId);
        json.addProperty("description", description);
        json.add("description_vector", gson.toJsonTree(vector));

        InsertReq insertReq = InsertReq.builder()
                .collectionName(COLLECTION_NAME)
                .data(Collections.singletonList(json))
                .build();

        milvusClient.insert(insertReq);
    }

    /**
     * 按 pk 删除一条记录（pk = objectType:sourceId 的约定由上层保证一致）。
     */
    public void deleteByPk(String pk) {
        ensureCollection();
        DeleteReq deleteReq = DeleteReq.builder()
                .collectionName(COLLECTION_NAME)
                .ids(Collections.singletonList(pk))
                .build();
        milvusClient.delete(deleteReq);
    }

    /**
     * 向量检索：返回 Milvus 原始 SearchResp（由上层负责解析为 DTO 并做业务过滤）。
     */
    public SearchResp search(float[] queryVector,
                             int topK,
                             String filterExpr,
                             List<String> outputFields) {
        ensureCollection();

        SearchReq req = SearchReq.builder()
                .collectionName(COLLECTION_NAME)
                .data(Collections.singletonList(new FloatVec(queryVector)))
                .topK(topK)
                .metricType(IndexParam.MetricType.COSINE)
                .filter(filterExpr)
                .outputFields(outputFields)
                .build();

        return milvusClient.search(req);
    }
}