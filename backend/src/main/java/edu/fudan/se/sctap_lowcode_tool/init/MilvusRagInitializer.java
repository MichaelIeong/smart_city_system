package edu.fudan.se.sctap_lowcode_tool.init;

import edu.fudan.se.sctap_lowcode_tool.utils.milvus.MilvusUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MilvusRagInitializer implements ApplicationRunner {

    private final MilvusUtil milvusUtil;

    @Override
    public void run(ApplicationArguments args) {
        // 启动时全量同步一次
        milvusUtil.syncDevicesToMilvus();
        milvusUtil.syncSpacesToMilvus();
        milvusUtil.syncRulesToMilvus();
        System.out.println("Milvus RAG 初始化完成");
    }
}