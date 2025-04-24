package edu.fudan.se.sctap_lowcode_tool;

import com.alibaba.dashscope.exception.NoApiKeyException;
import edu.fudan.se.sctap_lowcode_tool.repository.AppRuleRepository;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.MilvusUtil;
import edu.fudan.se.sctap_lowcode_tool.utils.milvus.entity.AppRuleRecord;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class MilvusTest {
    @Resource
    private MilvusUtil milvusUtil;

    @Resource
    private AppRuleRepository appRuleRepository;
    @Test
    public void test() throws NoApiKeyException {
//        milvusUtil.createCollection();
//        List<AppRuleRecord> records = new ArrayList<>();
//        records.add(new AppRuleRecord("1", "检测并处理车辆违规停车事件"));
//        records.add(new AppRuleRecord("2", "车辆进入停车场时自动执行相应服务"));
//        records.add(new AppRuleRecord("3", "车辆离开停车场时自动执行相应服务"));
//        for(AppRuleRecord record : records){
//            milvusUtil.insertRecord(record);
//        }
        List<AppRuleRecord> records = milvusUtil.queryVector("车辆进入停车场", 1);
        for(AppRuleRecord record : records){
            System.out.println(record.toString());
//            AppRuleInfo appRuleInfo = appRuleRepository.findById(Integer.parseInt(record.getId())).get();
//            System.out.println(appRuleInfo.getId());
//            System.out.println(appRuleInfo.getDescription());
//            System.out.println(appRuleInfo.getRuleJson());
//            System.out.println(appRuleInfo.getUpdateTime());
        }
//        milvusUtil.deleteRecordById("1");
    }
}












