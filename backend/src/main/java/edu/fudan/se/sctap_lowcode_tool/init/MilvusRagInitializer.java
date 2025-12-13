package edu.fudan.se.sctap_lowcode_tool.init;

import edu.fudan.se.sctap_lowcode_tool.service.FusionRuleRecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MilvusRagInitializer implements ApplicationRunner {

    private final FusionRuleRecommendService fusionRuleRecommendService;

    @Override
    public void run(ApplicationArguments args) {
        fusionRuleRecommendService.syncAllToMilvus();
        System.out.println("Milvus RAG 初始化完成");
    }
}