package edu.fudan.se.sctap_lowcode_tool.utils.milvus;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.fudan.se.sctap_lowcode_tool.model.AppRuleInfo;
import edu.fudan.se.sctap_lowcode_tool.repository.AppRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.entity.AppRuleRecord;
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
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MilvusUtil {
    private static final String COLLECTION_NAME = "tap_collection";
    private static final int VECTOR_DIMENSION = 1024;
    private final MilvusClientV2 milvusClient;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Resource
    private AppRuleRepository appRuleRepository;

    public MilvusUtil(MilvusClientV2 milvusClient) {
        this.milvusClient = milvusClient;
    }

    /**
     * 创建一个Collection
     * */
    public void createCollection() {
        CreateCollectionReq.CollectionSchema schema = milvusClient.createSchema();

        schema.addField(AddFieldReq.builder()
                .fieldName("id")
                .dataType(DataType.VarChar)
                .isPrimaryKey(true)
                .autoID(false)
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

        CreateCollectionReq createCollectionReq = CreateCollectionReq.builder()
                .collectionName(COLLECTION_NAME)
                .collectionSchema(schema)
                .indexParams(Collections.singletonList(indexParam))
                .build();

        milvusClient.createCollection(createCollectionReq);
    }

    /**
     * 往collection中插入一条数据
     */
    public void insertRecord(AppRuleRecord record) throws NoApiKeyException {
        // 判断collection是否存在，不存在则创建
        if(!milvusClient.hasCollection(HasCollectionReq.builder().collectionName(COLLECTION_NAME).build())){
            createCollection();
            // 将数据库中的数据插入
            List<AppRuleInfo> appRuleInfos = appRuleRepository.findAll();
            for (AppRuleInfo appRuleInfo : appRuleInfos) {
                AppRuleRecord appRuleRecord = new AppRuleRecord(appRuleInfo.getId().toString(), appRuleInfo.getDescription());
                insertRecord(appRuleRecord);
            }
        }
        JsonObject vector = new JsonObject();
        vector.addProperty("id", record.getId());
        vector.addProperty("description", record.getDescription());
        // 调用阿里向量模型服务，对描述进行向量化
        TextEmbeddingParam param = TextEmbeddingParam
                .builder()
                .model(TextEmbedding.Models.TEXT_EMBEDDING_V3)
                .apiKey(apiKey)
                .texts(Collections.singleton(record.getDescription())).build();
        TextEmbedding textEmbedding = new TextEmbedding();
        TextEmbeddingResult result = textEmbedding.call(param);
        List<Double> vectorList = result.getOutput().getEmbeddings().get(0).getEmbedding();
        // 转换为 float[]
        float[] floatArray = new float[vectorList.size()];
        for (int i = 0; i < vectorList.size(); i++) {
            floatArray[i] = vectorList.get(i).floatValue();
        }
        Gson gson = new Gson();
        vector.add("description_vector", gson.toJsonTree(floatArray));
        InsertReq insertReq = InsertReq.builder()
                .collectionName(COLLECTION_NAME)
                .data(Collections.singletonList(vector))
                .build();
        InsertResp resp = milvusClient.insert(insertReq);
    }

    /**
     * 按照向量检索，找到相似度最近的topK
     */
    public List<AppRuleRecord> queryVector(String queryText, int k) throws NoApiKeyException {
        // 判断collection是否存在，不存在则创建
        if(!milvusClient.hasCollection(HasCollectionReq.builder().collectionName(COLLECTION_NAME).build())){
            createCollection();
            // 将数据库中的数据插入
            List<AppRuleInfo> appRuleInfos = appRuleRepository.findAll();
            for (AppRuleInfo appRuleInfo : appRuleInfos) {
                AppRuleRecord appRuleRecord = new AppRuleRecord(appRuleInfo.getId().toString(), appRuleInfo.getDescription());
                insertRecord(appRuleRecord);
            }
        }
        // 调用阿里向量模型服务，对查询条件进行向量化
        TextEmbeddingParam param = TextEmbeddingParam
                .builder()
                .model(TextEmbedding.Models.TEXT_EMBEDDING_V3)
                .apiKey(apiKey)
                .texts(Collections.singleton(queryText)).build();
        TextEmbedding textEmbedding = new TextEmbedding();
        TextEmbeddingResult result = textEmbedding.call(param);
        List<Double> vectorList = result.getOutput().getEmbeddings().get(0).getEmbedding();
        // Double -> float
        float[] floatArray = new float[vectorList.size()];
        for (int i = 0; i < vectorList.size(); i++) {
            floatArray[i] = vectorList.get(i).floatValue();
        }
        // 执行向量搜索
        SearchResp searchResp = milvusClient.search(SearchReq.builder()
                .collectionName(COLLECTION_NAME)
                .data(Collections.singletonList(new FloatVec(floatArray)))
                .topK(k)
                .metricType(IndexParam.MetricType.COSINE)
                .outputFields(Arrays.asList("id", "description"))
                .build());
        // 转换结果
        List<AppRuleRecord> resultRecords = new ArrayList<>();
        for(List<SearchResp.SearchResult> searchResultList : searchResp.getSearchResults()){
            for(SearchResp.SearchResult searchResult : searchResultList){
                // 从搜索结果中提取实体字段
                Map<String, Object> entity = searchResult.getEntity();
                AppRuleRecord record = new AppRuleRecord((String) entity.get("id"), (String) entity.get("description"), searchResult.getScore());
                resultRecords.add(record);
            }
        }
        return resultRecords;
    }

    /**
     * 根据 id 删除一条记录
     */
    public void deleteRecordById(String id) {
        DeleteReq deleteReq = DeleteReq.builder()
                .collectionName(COLLECTION_NAME)
                .ids(Collections.singletonList(id))
                .build();
        milvusClient.delete(deleteReq);
    }
}